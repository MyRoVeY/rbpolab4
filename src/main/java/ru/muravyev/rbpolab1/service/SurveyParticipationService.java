package ru.muravyev.rbpolab1.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.muravyev.rbpolab1.entity.*;
import ru.muravyev.rbpolab1.exception.NotFoundException;
import ru.muravyev.rbpolab1.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class SurveyParticipationService {

    private final SurveyParticipationRepository participationRepository;
    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final SurveyQuestionRepository questionRepository;
    private final SurveyAnswerRepository answerRepository;

    public SurveyParticipationService(SurveyParticipationRepository participationRepository,
                                      SurveyRepository surveyRepository,
                                      UserRepository userRepository,
                                      SurveyQuestionRepository questionRepository,
                                      SurveyAnswerRepository answerRepository) {
        this.participationRepository = participationRepository;
        this.surveyRepository = surveyRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    @Transactional(readOnly = true)
    public List<SurveyParticipation> getAll() {
        return participationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SurveyParticipation getById(Long id) {
        return participationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Participation " + id + " not found"));
    }

    public SurveyParticipation create(Long surveyId, Long respondentId) {
        Survey survey = findSurvey(surveyId);
        User respondent = findUser(respondentId);
        checkSurveyOpen(survey);
        if (participationRepository.existsBySurvey_IdAndRespondent_Id(surveyId, respondentId)) {
            throw new IllegalStateException("User " + respondentId
                    + " already participates in survey " + surveyId);
        }

        SurveyParticipation participation = new SurveyParticipation();
        participation.setSurvey(survey);
        participation.setRespondent(respondent);
        participation.setStatus(ParticipationStatus.IN_PROGRESS);
        participation.setStartedAt(LocalDateTime.now());
        return participationRepository.save(participation);
    }

    public SurveyParticipation update(Long id, ParticipationStatus newStatus) {
        SurveyParticipation participation = getById(id);
        if (newStatus == null || newStatus == participation.getStatus()) {
            return participation;
        }
        if (newStatus != ParticipationStatus.ABANDONED) {
            throw new IllegalArgumentException(
                    "Only ABANDONED can be set via PATCH; use /submit to submit");
        }
        if (participation.getStatus() != ParticipationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Participation " + id + " is already "
                    + participation.getStatus());
        }
        participation.setStatus(ParticipationStatus.ABANDONED);
        return participation;
    }

    public void delete(Long id) {
        participationRepository.delete(getById(id));
    }

    public SurveyParticipation submit(Long id) {
        SurveyParticipation participation = getById(id);
        if (participation.getStatus() != ParticipationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Participation " + id + " is already "
                    + participation.getStatus());
        }

        Long surveyId = participation.getSurvey().getId();
        Survey survey = surveyRepository.findByIdForUpdate(surveyId)
                .orElseThrow(() -> new NotFoundException("Survey " + surveyId + " not found"));
        if (survey.getStatus() != SurveyStatus.ACTIVE) {
            throw new IllegalStateException("Survey " + surveyId + " is not active");
        }
        LocalDateTime now = LocalDateTime.now();
        if (survey.getEndsAt() != null && !now.isBefore(survey.getEndsAt())) {
            throw new IllegalStateException("The survey deadline has passed");
        }

        Set<Long> answered = answerRepository.findByParticipation_Id(id).stream()
                .map(a -> a.getQuestion().getId())
                .collect(Collectors.toSet());
        List<Long> missing = questionRepository.findBySurvey_IdAndRequiredTrue(surveyId).stream()
                .map(SurveyQuestion::getId)
                .filter(questionId -> !answered.contains(questionId))
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Required questions are not answered: " + missing);
        }

        participation.setStatus(ParticipationStatus.SUBMITTED);
        participation.setSubmittedAt(now);
        return participation;
    }

    private void checkSurveyOpen(Survey survey) {
        if (survey.getStatus() != SurveyStatus.ACTIVE) {
            throw new IllegalStateException("Survey " + survey.getId() + " is not active");
        }
        LocalDateTime now = LocalDateTime.now();
        if (survey.getStartsAt() != null && now.isBefore(survey.getStartsAt())) {
            throw new IllegalStateException("Survey " + survey.getId() + " has not started yet");
        }
        if (survey.getEndsAt() != null && !now.isBefore(survey.getEndsAt())) {
            throw new IllegalStateException("Survey " + survey.getId() + " is already over");
        }
    }

    private Survey findSurvey(Long surveyId) {
        if (surveyId == null) {
            throw new IllegalArgumentException("surveyId is required");
        }
        return surveyRepository.findById(surveyId)
                .orElseThrow(() -> new NotFoundException("Survey " + surveyId + " not found"));
    }

    private User findUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("respondentId is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
    }
}
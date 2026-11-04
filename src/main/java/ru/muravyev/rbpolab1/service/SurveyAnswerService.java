package ru.muravyev.rbpolab1.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.muravyev.rbpolab1.entity.ParticipationStatus;
import ru.muravyev.rbpolab1.entity.SurveyAnswer;
import ru.muravyev.rbpolab1.entity.SurveyParticipation;
import ru.muravyev.rbpolab1.entity.SurveyQuestion;
import ru.muravyev.rbpolab1.exception.NotFoundException;
import ru.muravyev.rbpolab1.repository.SurveyAnswerRepository;
import ru.muravyev.rbpolab1.repository.SurveyParticipationRepository;
import ru.muravyev.rbpolab1.repository.SurveyQuestionRepository;
import ru.muravyev.rbpolab1.security.CurrentUser;

import java.util.List;

@Service
@Transactional
public class SurveyAnswerService {

    private final SurveyAnswerRepository answerRepository;
    private final SurveyParticipationRepository participationRepository;
    private final SurveyQuestionRepository questionRepository;
    private final CurrentUser currentUser;

    public SurveyAnswerService(SurveyAnswerRepository answerRepository,
                               SurveyParticipationRepository participationRepository,
                               SurveyQuestionRepository questionRepository,
                               CurrentUser currentUser) {
        this.answerRepository = answerRepository;
        this.participationRepository = participationRepository;
        this.questionRepository = questionRepository;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public List<SurveyAnswer> getAll() {
        if (currentUser.isAdmin()) {
            return answerRepository.findAll();
        }
        Long me = currentUser.id();
        return answerRepository.findByParticipation_Respondent_IdOrParticipation_Survey_Author_Id(me, me);
    }

    @Transactional(readOnly = true)
    public SurveyAnswer getById(Long id) {
        return answerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Answer " + id + " not found"));
    }

    public SurveyAnswer create(Long participationId, Long questionId, String value) {
        SurveyParticipation participation = findParticipation(participationId);
        SurveyQuestion question = findQuestion(questionId);

        checkInProgress(participation);
        if (!question.getSurvey().getId().equals(participation.getSurvey().getId())) {
            throw new IllegalArgumentException("Question " + questionId
                    + " does not belong to the survey of participation " + participationId);
        }
        if (answerRepository.existsByParticipation_IdAndQuestion_Id(participationId, questionId)) {
            throw new IllegalStateException("Question " + questionId
                    + " is already answered in participation " + participationId);
        }
        checkValue(question, value);

        SurveyAnswer answer = new SurveyAnswer();
        answer.setParticipation(participation);
        answer.setQuestion(question);
        answer.setValue(value);
        return answerRepository.save(answer);
    }

    public SurveyAnswer update(Long id, String value) {
        SurveyAnswer answer = getById(id);
        if (value == null) {
            return answer;
        }
        checkInProgress(answer.getParticipation());
        checkValue(answer.getQuestion(), value);
        answer.setValue(value);
        return answerRepository.save(answer);
    }

    public void delete(Long id) {
        SurveyAnswer answer = getById(id);
        checkInProgress(answer.getParticipation());
        answerRepository.delete(answer);
    }

    private SurveyParticipation findParticipation(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("participationId is required");
        }
        return participationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Participation " + id + " not found"));
    }

    private SurveyQuestion findQuestion(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("questionId is required");
        }
        return questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Question " + id + " not found"));
    }

    private void checkInProgress(SurveyParticipation participation) {
        if (participation.getStatus() != ParticipationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Participation " + participation.getId()
                    + " is " + participation.getStatus() + ", answers cannot be changed");
        }
    }

    private void checkValue(SurveyQuestion question, String value) {
        if (value == null || !question.getOptions().contains(value)) {
            throw new IllegalArgumentException("Value must be one of " + question.getOptions());
        }
    }
}
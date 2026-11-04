package ru.muravyev.rbpolab1.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.muravyev.rbpolab1.entity.*;
import ru.muravyev.rbpolab1.exception.NotFoundException;
import ru.muravyev.rbpolab1.repository.*;
import ru.muravyev.rbpolab1.security.CurrentUser;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final SurveyQuestionRepository questionRepository;
    private final SurveyParticipationRepository participationRepository;
    private final SurveyAnswerRepository answerRepository;
    private final CurrentUser currentUser;

    public SurveyService(SurveyRepository surveyRepository,
                         UserRepository userRepository,
                         SurveyQuestionRepository questionRepository,
                         SurveyParticipationRepository participationRepository,
                         SurveyAnswerRepository answerRepository,
                         CurrentUser currentUser) {
        this.surveyRepository = surveyRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.participationRepository = participationRepository;
        this.answerRepository = answerRepository;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public List<Survey> getAll() {
        return surveyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Survey getById(Long id) {
        return surveyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Survey " + id + " not found"));
    }

    public Survey create(String title, LocalDateTime startsAt,
                         LocalDateTime endsAt, SurveyStatus status) {
        if (status != null && status != SurveyStatus.DRAFT) {
            throw new IllegalArgumentException(
                    "A new survey is always created as DRAFT; use /publish to activate it");
        }
        checkTitle(title);
        checkDates(startsAt, endsAt);

        Survey survey = new Survey();
        survey.setTitle(title);
        survey.setAuthor(currentUser.get()); // автор - тот, кто вошёл
        survey.setStartsAt(startsAt);
        survey.setEndsAt(endsAt);
        survey.setStatus(SurveyStatus.DRAFT);
        survey.setCreatedAt(LocalDateTime.now());
        return surveyRepository.save(survey);
    }

    public Survey update(Long id, String title, Long authorId, LocalDateTime startsAt,
                         LocalDateTime endsAt, SurveyStatus status) {
        Survey survey = getById(id);
        if (status != null && status != survey.getStatus()) {
            throw new IllegalArgumentException("Status is changed only by /publish and /close");
        }
        if (title != null) {
            checkTitle(title);
            survey.setTitle(title);
        }
        if (authorId != null && !authorId.equals(survey.getAuthor().getId())) {
            if (!currentUser.isAdmin()) {
                throw new AccessDeniedException("Only an administrator can change the survey author");
            }
            survey.setAuthor(findUser(authorId));
        }
        if (startsAt != null) {
            survey.setStartsAt(startsAt);
        }
        if (endsAt != null) {
            survey.setEndsAt(endsAt);
        }
        checkDates(survey.getStartsAt(), survey.getEndsAt());
        return surveyRepository.save(survey);
    }

    public void delete(Long id) {
        surveyRepository.delete(getById(id));
    }

    public Survey publish(Long id) {
        Survey survey = getById(id);
        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new IllegalStateException("Only a DRAFT survey can be published, current status: "
                    + survey.getStatus());
        }
        if (questionRepository.countBySurvey_Id(id) == 0) {
            throw new IllegalStateException("A survey without questions cannot be published");
        }
        LocalDateTime now = LocalDateTime.now();
        if (survey.getEndsAt() != null && !survey.getEndsAt().isAfter(now)) {
            throw new IllegalStateException("The survey end date has already passed");
        }
        if (survey.getStartsAt() == null) {
            survey.setStartsAt(now);
        }
        survey.setStatus(SurveyStatus.ACTIVE);
        return survey;
    }

    public SurveyCloseResult close(Long id) {
        Survey survey = surveyRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Survey " + id + " not found"));
        if (survey.getStatus() != SurveyStatus.ACTIVE) {
            throw new IllegalStateException("Only an ACTIVE survey can be closed, current status: "
                    + survey.getStatus());
        }
        List<SurveyParticipation> unfinished =
                participationRepository.findBySurvey_IdAndStatus(id, ParticipationStatus.IN_PROGRESS);
        for (SurveyParticipation p : unfinished) {
            p.setStatus(ParticipationStatus.ABANDONED);
        }
        survey.setStatus(SurveyStatus.CLOSED);
        return new SurveyCloseResult(survey, unfinished.size());
    }

    public Survey copy(Long id) {
        Survey source = getById(id);
        User author = currentUser.get();

        String title = "Копия: " + source.getTitle();
        if (title.length() > 200) {
            title = title.substring(0, 200);
        }

        Survey copy = new Survey();
        copy.setTitle(title);
        copy.setAuthor(author);
        copy.setStatus(SurveyStatus.DRAFT);
        copy.setCreatedAt(LocalDateTime.now());
        surveyRepository.save(copy);

        for (SurveyQuestion q : questionRepository.findBySurvey_IdOrderByIdAsc(id)) {
            SurveyQuestion newQuestion = new SurveyQuestion();
            newQuestion.setSurvey(copy);
            newQuestion.setText(q.getText());
            newQuestion.setRequired(q.isRequired());
            newQuestion.setOptions(new ArrayList<>(q.getOptions()));
            questionRepository.save(newQuestion);
        }
        return copy;
    }

    @Transactional(readOnly = true)
    public SurveyResults results(Long id) {
        Survey survey = getById(id);
        if (survey.getStatus() == SurveyStatus.DRAFT) {
            throw new IllegalStateException("A DRAFT survey has no results");
        }
        long submitted = participationRepository.countBySurvey_IdAndStatus(id, ParticipationStatus.SUBMITTED);

        Map<Long, Map<String, Long>> counts = new HashMap<>();
        for (AnswerCountView row : answerRepository.countAnswers(id, ParticipationStatus.SUBMITTED)) {
            counts.computeIfAbsent(row.getQuestionId(), k -> new HashMap<>())
                    .put(row.getOptionValue(), row.getTotal());
        }

        List<SurveyResults.QuestionStats> stats = new ArrayList<>();
        for (SurveyQuestion q : questionRepository.findBySurvey_IdOrderByIdAsc(id)) {
            Map<String, Long> raw = counts.getOrDefault(q.getId(), Map.of());
            Map<String, Long> byOption = new LinkedHashMap<>();
            for (String option : q.getOptions()) {
                byOption.put(option, raw.getOrDefault(option, 0L)); // невыбранные варианты = 0
            }
            stats.add(new SurveyResults.QuestionStats(q, byOption));
        }
        return new SurveyResults(survey, submitted, stats);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
    }

    private void checkTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
    }

    private void checkDates(LocalDateTime startsAt, LocalDateTime endsAt) {
        if (startsAt != null && endsAt != null && !endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("endsAt must be after startsAt");
        }
    }
}
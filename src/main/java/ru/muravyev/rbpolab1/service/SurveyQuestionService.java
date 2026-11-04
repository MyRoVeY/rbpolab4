package ru.muravyev.rbpolab1.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.muravyev.rbpolab1.entity.Survey;
import ru.muravyev.rbpolab1.entity.SurveyQuestion;
import ru.muravyev.rbpolab1.exception.NotFoundException;
import ru.muravyev.rbpolab1.repository.SurveyQuestionRepository;
import ru.muravyev.rbpolab1.repository.SurveyRepository;
import ru.muravyev.rbpolab1.entity.SurveyStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class SurveyQuestionService {

    private final SurveyQuestionRepository questionRepository;
    private final SurveyRepository surveyRepository;

    public SurveyQuestionService(SurveyQuestionRepository questionRepository,
                                 SurveyRepository surveyRepository) {
        this.questionRepository = questionRepository;
        this.surveyRepository = surveyRepository;
    }

    @Transactional(readOnly = true)
    public List<SurveyQuestion> getAll() {
        return questionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SurveyQuestion getById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Question " + id + " not found"));
    }

    public SurveyQuestion create(Long surveyId, String text, Boolean required, List<String> options) {
        Survey survey = findSurvey(surveyId);
        checkDraft(survey);
        checkText(text);
        checkOptions(options);

        SurveyQuestion question = new SurveyQuestion();
        question.setSurvey(survey);
        question.setText(text);
        question.setRequired(required != null && required);
        question.setOptions(new ArrayList<>(options));
        return questionRepository.save(question);
    }

    public SurveyQuestion update(Long id, Long surveyId, String text, Boolean required, List<String> options) {
        SurveyQuestion question = getById(id);
        checkDraft(question.getSurvey());
        if (surveyId != null) {
            Survey target = findSurvey(surveyId);
            checkDraft(target);
            question.setSurvey(target);
        }
        if (surveyId != null) {
            question.setSurvey(findSurvey(surveyId));
        }
        if (text != null) {
            checkText(text);
            question.setText(text);
        }
        if (required != null) {
            question.setRequired(required);
        }
        if (options != null) {
            checkOptions(options);
            question.setOptions(new ArrayList<>(options));
        }
        return questionRepository.save(question);
    }

    public void delete(Long id) {
        SurveyQuestion question = getById(id);
        checkDraft(question.getSurvey());
        // ответы на вопрос удалит БД (ON DELETE CASCADE)
        questionRepository.delete(question);
    }

    private Survey findSurvey(Long surveyId) {
        if (surveyId == null) {
            throw new IllegalArgumentException("surveyId is required");
        }
        return surveyRepository.findById(surveyId)
                .orElseThrow(() -> new NotFoundException("Survey " + surveyId + " not found"));
    }

    private void checkText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text is required");
        }
    }

    private void checkOptions(List<String> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("options must contain at least one value");
        }
        for (String option : options) {
            if (option == null || option.isBlank()) {
                throw new IllegalArgumentException("options must not contain empty values");
            }
        }
        if (new HashSet<>(options).size() != options.size()) {
            throw new IllegalArgumentException("options must not contain duplicates");
        }
    }
    private void checkDraft(Survey survey) {
        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new IllegalStateException("Questions can be changed only in a DRAFT survey; survey "
                    + survey.getId() + " is " + survey.getStatus());
        }
    }
}
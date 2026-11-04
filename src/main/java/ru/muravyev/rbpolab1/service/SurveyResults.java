package ru.muravyev.rbpolab1.service;

import ru.muravyev.rbpolab1.entity.Survey;
import ru.muravyev.rbpolab1.entity.SurveyQuestion;

import java.util.List;
import java.util.Map;

public record SurveyResults(Survey survey, long submittedParticipations, List<QuestionStats> questions) {

    public record QuestionStats(SurveyQuestion question, Map<String, Long> optionCounts) {
    }
}
package ru.muravyev.rbpolab1.dto;

import ru.muravyev.rbpolab1.entity.SurveyStatus;

import java.util.List;

public record SurveyResultsResponse(
        Long surveyId,
        String title,
        SurveyStatus status,
        long submittedParticipations,
        List<QuestionResultResponse> questions
) {
}
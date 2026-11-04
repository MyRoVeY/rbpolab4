package ru.muravyev.rbpolab1.dto;

public record SurveyCloseResponse(SurveyResponse survey, int abandonedParticipations) {
}
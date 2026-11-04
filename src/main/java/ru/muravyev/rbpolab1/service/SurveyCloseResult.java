package ru.muravyev.rbpolab1.service;

import ru.muravyev.rbpolab1.entity.Survey;

public record SurveyCloseResult(Survey survey, int abandonedParticipations) {
}
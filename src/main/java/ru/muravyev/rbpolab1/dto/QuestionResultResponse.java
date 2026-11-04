package ru.muravyev.rbpolab1.dto;

import java.util.Map;

public record QuestionResultResponse(
        Long questionId,
        String text,
        boolean required,
        Map<String, Long> optionCounts
) {
}
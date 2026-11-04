package ru.muravyev.rbpolab1.repository;

public interface AnswerCountView {

    Long getQuestionId();

    String getOptionValue();

    Long getTotal();
}
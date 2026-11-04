package ru.muravyev.rbpolab1.repository;

// "Проекция": одна строка результата группировки ответов
public interface AnswerCountView {

    Long getQuestionId();

    String getOptionValue();

    Long getTotal();
}
package ru.muravyev.rbpolab1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.muravyev.rbpolab1.entity.SurveyQuestion;

import java.util.List;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {

    List<SurveyQuestion> findBySurvey_IdOrderByIdAsc(Long surveyId);

    List<SurveyQuestion> findBySurvey_IdAndRequiredTrue(Long surveyId);

    long countBySurvey_Id(Long surveyId);
}
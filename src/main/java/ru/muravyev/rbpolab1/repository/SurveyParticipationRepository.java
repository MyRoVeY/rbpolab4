package ru.muravyev.rbpolab1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.muravyev.rbpolab1.entity.ParticipationStatus;
import ru.muravyev.rbpolab1.entity.SurveyParticipation;

import java.util.List;

public interface SurveyParticipationRepository extends JpaRepository<SurveyParticipation, Long> {

    boolean existsBySurvey_IdAndRespondent_Id(Long surveyId, Long respondentId);

    List<SurveyParticipation> findBySurvey_IdAndStatus(Long surveyId, ParticipationStatus status);

    long countBySurvey_IdAndStatus(Long surveyId, ParticipationStatus status);
}
package ru.muravyev.rbpolab1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.muravyev.rbpolab1.entity.ParticipationStatus;
import ru.muravyev.rbpolab1.entity.SurveyAnswer;

import java.util.List;

public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {

    boolean existsByParticipation_IdAndQuestion_Id(Long participationId, Long questionId);

    List<SurveyAnswer> findByParticipation_Id(Long participationId);

    @Query("""
            select a.question.id as questionId, a.value as optionValue, count(a) as total
            from SurveyAnswer a
            where a.participation.survey.id = :surveyId
              and a.participation.status = :status
            group by a.question.id, a.value
            """)
    List<AnswerCountView> countAnswers(@Param("surveyId") Long surveyId,
                                       @Param("status") ParticipationStatus status);

    List<SurveyAnswer> findByParticipation_Respondent_IdOrParticipation_Survey_Author_Id(
            Long respondentId, Long authorId);
}
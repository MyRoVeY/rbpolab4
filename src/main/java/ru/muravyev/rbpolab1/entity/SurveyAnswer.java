package ru.muravyev.rbpolab1.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_answers", schema = "app")
public class SurveyAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participation_id", nullable = false)
    private SurveyParticipation participation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion question;

    @Column(nullable = false, length = 200)
    private String value;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SurveyParticipation getParticipation() { return participation; }
    public void setParticipation(SurveyParticipation participation) { this.participation = participation; }

    public SurveyQuestion getQuestion() { return question; }
    public void setQuestion(SurveyQuestion question) { this.question = question; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
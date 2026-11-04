package ru.muravyev.rbpolab1.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_questions", schema = "app")
public class SurveyQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(nullable = false)
    private boolean required;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "question_options", schema = "app",
            joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "position")
    @Column(name = "value", nullable = false, length = 200)
    private List<String> options = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Survey getSurvey() { return survey; }
    public void setSurvey(Survey survey) { this.survey = survey; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
}
package ru.muravyev.rbpolab1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.muravyev.rbpolab1.dto.SurveyQuestionRequest;
import ru.muravyev.rbpolab1.dto.SurveyQuestionResponse;
import ru.muravyev.rbpolab1.entity.SurveyQuestion;
import ru.muravyev.rbpolab1.service.SurveyQuestionService;

import java.util.List;

@RestController
@RequestMapping("/api/survey-questions")
public class SurveyQuestionController {

    private final SurveyQuestionService questionService;

    public SurveyQuestionController(SurveyQuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<SurveyQuestionResponse> getAll() {
        return questionService.getAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public SurveyQuestionResponse getById(@PathVariable Long id) {
        return toResponse(questionService.getById(id));
    }

    @PostMapping
    @PreAuthorize("@access.canManageSurvey(#r.surveyId())")
    public ResponseEntity<SurveyQuestionResponse> create(@RequestBody SurveyQuestionRequest r) {
        SurveyQuestion created = questionService.create(r.surveyId(), r.text(), r.required(), r.options());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@access.canManageQuestion(#id) and @access.canManageSurvey(#r.surveyId())")
    public SurveyQuestionResponse update(@PathVariable Long id, @RequestBody SurveyQuestionRequest r) {
        return toResponse(questionService.update(id, r.surveyId(), r.text(), r.required(), r.options()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@access.canManageQuestion(#id)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private SurveyQuestionResponse toResponse(SurveyQuestion q) {
        return new SurveyQuestionResponse(q.getId(), q.getSurvey().getId(), q.getText(),
                q.isRequired(), q.getOptions());
    }
}
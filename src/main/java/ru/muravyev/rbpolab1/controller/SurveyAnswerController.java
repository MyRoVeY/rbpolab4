package ru.muravyev.rbpolab1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.muravyev.rbpolab1.dto.SurveyAnswerRequest;
import ru.muravyev.rbpolab1.dto.SurveyAnswerResponse;
import ru.muravyev.rbpolab1.entity.SurveyAnswer;
import ru.muravyev.rbpolab1.service.SurveyAnswerService;

import java.util.List;

@RestController
@RequestMapping("/api/survey-answers")
public class SurveyAnswerController {

    private final SurveyAnswerService answerService;

    public SurveyAnswerController(SurveyAnswerService answerService) {
        this.answerService = answerService;
    }

    @GetMapping
    public List<SurveyAnswerResponse> getAll() {
        return answerService.getAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public SurveyAnswerResponse getById(@PathVariable Long id) {
        return toResponse(answerService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SurveyAnswerResponse> create(@RequestBody SurveyAnswerRequest r) {
        SurveyAnswer created = answerService.create(r.participationId(), r.questionId(), r.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PatchMapping("/{id}")
    public SurveyAnswerResponse update(@PathVariable Long id, @RequestBody SurveyAnswerRequest r) {
        return toResponse(answerService.update(id, r.value()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        answerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private SurveyAnswerResponse toResponse(SurveyAnswer a) {
        return new SurveyAnswerResponse(a.getId(), a.getParticipation().getId(),
                a.getQuestion().getId(), a.getValue());
    }
}
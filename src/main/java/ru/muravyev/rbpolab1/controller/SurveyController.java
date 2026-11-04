package ru.muravyev.rbpolab1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.muravyev.rbpolab1.dto.SurveyRequest;
import ru.muravyev.rbpolab1.dto.SurveyResponse;
import ru.muravyev.rbpolab1.entity.Survey;
import ru.muravyev.rbpolab1.service.SurveyService;
import ru.muravyev.rbpolab1.dto.*;
import ru.muravyev.rbpolab1.service.SurveyCloseResult;
import ru.muravyev.rbpolab1.service.SurveyResults;

import java.util.List;

@RestController
@RequestMapping("/api/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping
    public List<SurveyResponse> getAll() {
        return surveyService.getAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public SurveyResponse getById(@PathVariable Long id) {
        return toResponse(surveyService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SurveyResponse> create(@RequestBody SurveyRequest r) {
        Survey created = surveyService.create(r.title(), r.authorId(),
                r.startsAt(), r.endsAt(), r.status());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PatchMapping("/{id}")
    public SurveyResponse update(@PathVariable Long id, @RequestBody SurveyRequest r) {
        return toResponse(surveyService.update(id, r.title(), r.authorId(),
                r.startsAt(), r.endsAt(), r.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        surveyService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/publish")
    public SurveyResponse publish(@PathVariable Long id) {
        return toResponse(surveyService.publish(id));
    }

    @PostMapping("/{id}/close")
    public SurveyCloseResponse close(@PathVariable Long id) {
        SurveyCloseResult result = surveyService.close(id);
        return new SurveyCloseResponse(toResponse(result.survey()), result.abandonedParticipations());
    }

    @PostMapping("/{id}/copy")
    public ResponseEntity<SurveyResponse> copy(@PathVariable Long id,
                                               @RequestBody SurveyCopyRequest request) {
        Survey copy = surveyService.copy(id, request.authorId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(copy));
    }

    @GetMapping("/{id}/results")
    public SurveyResultsResponse results(@PathVariable Long id) {
        SurveyResults r = surveyService.results(id);
        List<QuestionResultResponse> questions = r.questions().stream()
                .map(qs -> new QuestionResultResponse(
                        qs.question().getId(),
                        qs.question().getText(),
                        qs.question().isRequired(),
                        qs.optionCounts()))
                .toList();
        return new SurveyResultsResponse(r.survey().getId(), r.survey().getTitle(),
                r.survey().getStatus(), r.submittedParticipations(), questions);
    }

    private SurveyResponse toResponse(Survey s) {
        return new SurveyResponse(s.getId(), s.getTitle(), s.getAuthor().getId(),
                s.getStartsAt(), s.getEndsAt(), s.getStatus(), s.getCreatedAt());
    }
}
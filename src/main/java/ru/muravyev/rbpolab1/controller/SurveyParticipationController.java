package ru.muravyev.rbpolab1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.muravyev.rbpolab1.dto.SurveyParticipationRequest;
import ru.muravyev.rbpolab1.dto.SurveyParticipationResponse;
import ru.muravyev.rbpolab1.entity.SurveyParticipation;
import ru.muravyev.rbpolab1.service.SurveyParticipationService;

import java.util.List;

@RestController
@RequestMapping("/api/survey-participations")
public class SurveyParticipationController {

    private final SurveyParticipationService participationService;

    public SurveyParticipationController(SurveyParticipationService participationService) {
        this.participationService = participationService;
    }

    @GetMapping
    public List<SurveyParticipationResponse> getAll() {
        return participationService.getAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public SurveyParticipationResponse getById(@PathVariable Long id) {
        return toResponse(participationService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SurveyParticipationResponse> create(@RequestBody SurveyParticipationRequest r) {
        SurveyParticipation created = participationService.create(r.surveyId(), r.respondentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PatchMapping("/{id}")
    public SurveyParticipationResponse update(@PathVariable Long id,
                                              @RequestBody SurveyParticipationRequest r) {
        return toResponse(participationService.update(id, r.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        participationService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/submit")
    public SurveyParticipationResponse submit(@PathVariable Long id) {
        return toResponse(participationService.submit(id));
    }

    private SurveyParticipationResponse toResponse(SurveyParticipation p) {
        return new SurveyParticipationResponse(p.getId(), p.getSurvey().getId(),
                p.getRespondent().getId(), p.getStatus(), p.getStartedAt(), p.getSubmittedAt());
    }
}
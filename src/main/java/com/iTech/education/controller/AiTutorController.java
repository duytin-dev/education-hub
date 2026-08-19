package com.iTech.education.controller;

import com.iTech.education.dto.request.AiExplainRequest;
import com.iTech.education.dto.request.AiGradeRequest;
import com.iTech.education.dto.request.AiQuizRequest;
import com.iTech.education.dto.response.ApiResponse;
import com.iTech.education.service.AiTutorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@Validated
public class AiTutorController {

    private final AiTutorService aiTutorService;

    public AiTutorController(AiTutorService aiTutorService) {
        this.aiTutorService = aiTutorService;
    }

    @PostMapping("/explain")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> explain(@Valid @RequestBody AiExplainRequest request, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(aiTutorService.explain(request, authentication.getName())));
    }

    @PostMapping("/quiz")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> quiz(@Valid @RequestBody AiQuizRequest request, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(aiTutorService.quiz(request, authentication.getName())));
    }

    @PostMapping("/grade")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> grade(@Valid @RequestBody AiGradeRequest request, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(aiTutorService.grade(request, authentication.getName())));
    }
}

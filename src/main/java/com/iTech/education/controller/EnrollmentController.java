package com.iTech.education.controller;

import com.iTech.education.dto.response.ApiResponse;
import com.iTech.education.dto.response.EnrollmentResponse;
import com.iTech.education.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> enroll(@PathVariable Long courseId, Authentication authentication) {
        EnrollmentResponse enrollment = enrollmentService.enroll(courseId, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký khóa học thành công", enrollment));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<?> getMyEnrollments(Authentication authentication) {
        List<EnrollmentResponse> enrollments = enrollmentService.getMyEnrollments(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }
}

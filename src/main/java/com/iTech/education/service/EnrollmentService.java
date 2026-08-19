package com.iTech.education.service;

import com.iTech.education.dto.response.EnrollmentResponse;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponse enroll(Long courseId, String currentUserEmail);

    List<EnrollmentResponse> getMyEnrollments(String currentUserEmail);

    EnrollmentResponse createEnrollmentForUser(Long userId, Long courseId);
}

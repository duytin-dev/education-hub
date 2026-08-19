package com.iTech.education.dto.response;

import com.iTech.education.entity.Enrollment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EnrollmentResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseThumbnail;
    private Double progressPercent;
    private Boolean isCompleted;
    private LocalDateTime enrolledAt;

    public static EnrollmentResponse fromEntity(Enrollment enrollment) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setProgressPercent(enrollment.getProgressPercent());
        response.setIsCompleted(enrollment.getIsCompleted());
        response.setEnrolledAt(enrollment.getEnrolledAt());

        if (enrollment.getCourse() != null) {
            response.setCourseId(enrollment.getCourse().getId());
            response.setCourseTitle(enrollment.getCourse().getTitle());
            response.setCourseThumbnail(enrollment.getCourse().getThumbnail());
        }

        return response;
    }
}

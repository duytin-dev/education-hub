package com.iTech.education.service.impl;

import com.iTech.education.dto.request.ProgressUpdateRequest;
import com.iTech.education.dto.response.LessonProgressResponse;
import com.iTech.education.entity.Enrollment;
import com.iTech.education.entity.Lesson;
import com.iTech.education.entity.LessonProgress;
import com.iTech.education.entity.User;
import com.iTech.education.exception.ResourceNotFoundException;
import com.iTech.education.repository.EnrollmentRepository;
import com.iTech.education.repository.LessonProgressRepository;
import com.iTech.education.repository.LessonRepository;
import com.iTech.education.service.ProgressService;
import com.iTech.education.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProgressServiceImpl implements ProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;

    public ProgressServiceImpl(LessonProgressRepository lessonProgressRepository,
                               LessonRepository lessonRepository,
                               EnrollmentRepository enrollmentRepository,
                               UserService userService) {
        this.lessonProgressRepository = lessonProgressRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public LessonProgressResponse updateLessonProgress(Long lessonId,
                                                       ProgressUpdateRequest request,
                                                       String currentUserEmail) {
        User user = userService.handleGetUserByUsername(currentUserEmail);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với id: " + lessonId));

        Long courseId = lesson.getCourse().getId();
        enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new AccessDeniedException("Bạn chưa đăng ký khóa học này"));

        LessonProgress progress = lessonProgressRepository.findByUserIdAndLessonId(user.getId(), lessonId)
                .orElseGet(() -> {
                    LessonProgress created = new LessonProgress();
                    created.setUser(user);
                    created.setLesson(lesson);
                    created.setIsCompleted(false);
                    return created;
                });

        if (request.getIsCompleted() != null) {
            progress.setIsCompleted(request.getIsCompleted());
            progress.setCompletedAt(Boolean.TRUE.equals(request.getIsCompleted()) ? LocalDateTime.now() : null);
        }

        if (request.getWatchedDuration() != null) {
            progress.setWatchedDuration(request.getWatchedDuration());
        }

        LessonProgress saved = lessonProgressRepository.save(progress);
        Double courseProgressPercent = recalculateEnrollmentProgress(user.getId(), courseId);

        return LessonProgressResponse.fromEntity(saved, courseProgressPercent);
    }

    private Double recalculateEnrollmentProgress(Long userId, Long courseId) {
        long totalLessons = lessonRepository.countByCourseId(courseId);
        if (totalLessons == 0) {
            return 0.0;
        }

        long completedLessons = lessonProgressRepository
                .countByUserIdAndLesson_CourseIdAndIsCompletedTrue(userId, courseId);

        double percent = (completedLessons * 100.0) / totalLessons;
        boolean completed = completedLessons >= totalLessons;

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy enrollment"));

        enrollment.setProgressPercent(percent);
        enrollment.setIsCompleted(completed);
        enrollmentRepository.save(enrollment);

        return percent;
    }
}

package com.iTech.education.service.impl;

import com.iTech.education.dto.response.EnrollmentResponse;
import com.iTech.education.entity.Course;
import com.iTech.education.entity.Enrollment;
import com.iTech.education.entity.User;
import com.iTech.education.exception.EnrollmentConflictException;
import com.iTech.education.exception.ResourceNotFoundException;
import com.iTech.education.repository.CourseRepository;
import com.iTech.education.repository.EnrollmentRepository;
import com.iTech.education.repository.UserRepository;
import com.iTech.education.service.EnrollmentService;
import com.iTech.education.utils.CourseStatus;
import com.iTech.education.utils.RoleType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,
                                 CourseRepository courseRepository,
                                 UserRepository userRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public EnrollmentResponse enroll(Long courseId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (user.getRole() != RoleType.STUDENT) {
            throw new AccessDeniedException("Chỉ học viên mới được đăng ký khóa học");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với id: " + courseId));

        if (course.getInstructor().getId().equals(user.getId())) {
            throw new AccessDeniedException("Giảng viên không cần đăng ký khóa học của chính mình");
        }

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException("Khóa học chưa được publish");
        }

        if (course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("Khóa học trả phí, vui lòng thanh toán trước");
        }

        return createEnrollmentForUser(user.getId(), courseId);
    }

    @Override
    public List<EnrollmentResponse> getMyEnrollments(String currentUserEmail) {
        return enrollmentRepository.findByUser_EmailOrderByEnrolledAtDesc(currentUserEmail)
                .stream()
                .map(EnrollmentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public EnrollmentResponse createEnrollmentForUser(Long userId, Long courseId) {
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new EnrollmentConflictException("Bạn đã đăng ký khóa học này rồi");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với id: " + courseId));

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setProgressPercent(0.0);
        enrollment.setIsCompleted(false);

        Enrollment saved = enrollmentRepository.save(enrollment);
        return EnrollmentResponse.fromEntity(saved);
    }
}

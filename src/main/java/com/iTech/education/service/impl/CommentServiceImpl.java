package com.iTech.education.service.impl;

import com.iTech.education.dto.request.CommentRequest;
import com.iTech.education.dto.response.CommentResponse;
import com.iTech.education.entity.Comment;
import com.iTech.education.entity.Course;
import com.iTech.education.entity.User;
import com.iTech.education.exception.ResourceNotFoundException;
import com.iTech.education.repository.CommentRepository;
import com.iTech.education.repository.CourseRepository;
import com.iTech.education.repository.EnrollmentRepository;
import com.iTech.education.service.CommentService;
import com.iTech.education.service.UserService;
import com.iTech.education.utils.RoleType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;

    public CommentServiceImpl(CommentRepository commentRepository,
                              CourseRepository courseRepository,
                              EnrollmentRepository enrollmentRepository,
                              UserService userService) {
        this.commentRepository = commentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userService = userService;
    }

    @Override
    public List<CommentResponse> getByCourseId(Long courseId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với id: " + courseId));

        return commentRepository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream()
                .map(CommentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public CommentResponse create(Long courseId, CommentRequest request, String currentUserEmail) {
        User user = userService.handleGetUserByUsername(currentUserEmail);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với id: " + courseId));

        assertCanComment(user, course);

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setCourse(course);
        comment.setContent(request.getContent());
        comment.setRating(request.getRating());

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận gốc"));
            if (!parent.getCourse().getId().equals(courseId)) {
                throw new IllegalArgumentException("Bình luận gốc không thuộc khóa học này");
            }
            comment.setParent(parent);
        }

        Comment saved = commentRepository.save(comment);
        return CommentResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public CommentResponse update(Long commentId, CommentRequest request, String currentUserEmail) {
        User user = userService.handleGetUserByUsername(currentUserEmail);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận"));

        assertCanModifyComment(user, comment);

        comment.setContent(request.getContent());
        comment.setRating(request.getRating());

        Comment updated = commentRepository.save(comment);
        return CommentResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void delete(Long commentId, String currentUserEmail) {
        User user = userService.handleGetUserByUsername(currentUserEmail);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận"));

        assertCanModifyComment(user, comment);
        commentRepository.delete(comment);
    }

    private void assertCanComment(User user, Course course) {
        if (user.getRole() == RoleType.ADMIN) {
            return;
        }
        if (course.getInstructor().getId().equals(user.getId())) {
            return;
        }
        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
            return;
        }
        throw new AccessDeniedException("Bạn cần đăng ký khóa học để bình luận");
    }

    private void assertCanModifyComment(User user, Comment comment) {
        boolean isOwner = comment.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == RoleType.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Bạn không có quyền thao tác bình luận này");
        }
    }
}

package com.iTech.education.controller;

import com.iTech.education.dto.request.CommentRequest;
import com.iTech.education.dto.response.ApiResponse;
import com.iTech.education.dto.response.CommentResponse;
import com.iTech.education.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/api/v1/courses/{courseId}/comments")
    public ResponseEntity<?> getByCourse(@PathVariable Long courseId) {
        List<CommentResponse> comments = commentService.getByCourseId(courseId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @PostMapping("/api/v1/courses/{courseId}/comments")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<?> create(@PathVariable Long courseId,
                                    @Valid @RequestBody CommentRequest request,
                                    Authentication authentication) {
        CommentResponse comment = commentService.create(courseId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo bình luận thành công", comment));
    }

    @PutMapping("/api/v1/comments/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody CommentRequest request,
                                    Authentication authentication) {
        CommentResponse comment = commentService.update(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bình luận thành công", comment));
    }

    @DeleteMapping("/api/v1/comments/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        commentService.delete(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Xóa bình luận thành công", null));
    }
}

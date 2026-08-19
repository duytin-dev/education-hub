package com.iTech.education.service;

import com.iTech.education.dto.request.CommentRequest;
import com.iTech.education.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {

    List<CommentResponse> getByCourseId(Long courseId);

    CommentResponse create(Long courseId, CommentRequest request, String currentUserEmail);

    CommentResponse update(Long commentId, CommentRequest request, String currentUserEmail);

    void delete(Long commentId, String currentUserEmail);
}

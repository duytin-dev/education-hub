package com.iTech.education.service;

import com.iTech.education.dto.request.LessonRequest;
import com.iTech.education.dto.request.ProgressUpdateRequest;
import com.iTech.education.dto.response.LessonProgressResponse;
import com.iTech.education.dto.response.LessonResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LessonService {

    LessonResponse create(LessonRequest request, String currentUserEmail);

    LessonResponse update(Long id, LessonRequest request, String currentUserEmail);

    void delete(Long id, String currentUserEmail);

    LessonResponse getById(Long id, String currentUserEmail);

    List<LessonResponse> getByCourseId(Long courseId, String currentUserEmail);

    LessonResponse uploadVideo(Long id, MultipartFile file, String currentUserEmail) throws IOException;

    LessonProgressResponse updateProgress(Long lessonId, ProgressUpdateRequest request, String currentUserEmail);
}

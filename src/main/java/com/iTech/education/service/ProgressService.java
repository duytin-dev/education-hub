package com.iTech.education.service;

import com.iTech.education.dto.request.ProgressUpdateRequest;
import com.iTech.education.dto.response.LessonProgressResponse;

public interface ProgressService {

    LessonProgressResponse updateLessonProgress(Long lessonId, ProgressUpdateRequest request, String currentUserEmail);
}

package com.iTech.education.service;

import com.iTech.education.dto.request.LessonRequest;
import com.iTech.education.dto.response.LessonResponse;
import com.iTech.education.entity.Lesson;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LessonService {
   // Lesson findLessonById(Long idLesson);
   // Lesson saveLesson(Lesson lesson);
   LessonResponse create(LessonRequest request, String currentUserEmail);

    LessonResponse update(Long id, LessonRequest request, String currentUserEmail);

    void delete(Long id, String currentUserEmail);

    LessonResponse getById(Long id);

    List<LessonResponse> getByCourseId(Long courseId);

    LessonResponse uploadVideo(Long id, MultipartFile file, String currentUserEmail) throws IOException;
}

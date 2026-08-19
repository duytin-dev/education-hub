package com.iTech.education.controller;

import com.iTech.education.dto.request.LessonRequest;
import com.iTech.education.dto.request.ProgressUpdateRequest;
import com.iTech.education.dto.response.ApiResponse;
import com.iTech.education.dto.response.LessonResponse;
import com.iTech.education.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/lessons")
@Validated
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getByCourseId(@PathVariable Long courseId, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        List<LessonResponse> lessons = lessonService.getByCourseId(courseId, email);
        return ResponseEntity.ok(ApiResponse.success(lessons));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        LessonResponse lesson = lessonService.getById(id, email);
        return ResponseEntity.ok(ApiResponse.success(lesson));
    }

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> create(@Valid @RequestBody LessonRequest request, Authentication authentication) {
        LessonResponse created = lessonService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo bài học thành công", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody LessonRequest request,
                                    Authentication authentication) {
        LessonResponse updated = lessonService.update(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật bài học thành công", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        lessonService.delete(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Xóa bài học thành công", null));
    }

    @PutMapping(value = "/{id}/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> uploadVideo(@PathVariable Long id,
                                         @RequestParam("file") MultipartFile file,
                                         Authentication authentication) throws IOException {
        LessonResponse updated = lessonService.uploadVideo(id, file, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Upload video thành công", updated));
    }

    @PutMapping("/{id}/progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> updateProgress(@PathVariable Long id,
                                            @Valid @RequestBody ProgressUpdateRequest request,
                                            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật tiến độ thành công",
                lessonService.updateProgress(id, request, authentication.getName())
        ));
    }
}

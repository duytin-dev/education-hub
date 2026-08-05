package com.iTech.education.controller;

import com.iTech.education.entity.Lesson;
import com.iTech.education.service.CloudinaryService;
import com.iTech.education.service.LessonService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
@RestController
@RequestMapping("/api/v1")
@Validated
public class LessonController {
    private final CloudinaryService cloudinaryService;
    private final LessonService lessonService;
    public LessonController(LessonService lessonService,CloudinaryService cloudinaryService){
        this.cloudinaryService = cloudinaryService;
        this.lessonService = lessonService;
    }
//    @PostMapping(value="/lessons/{id}/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE
//    )
//    public ResponseEntity<?> uploadVideo(
//            @PathVariable Long id,
//            @RequestParam("file") MultipartFile file
//    ) throws IOException {
//        String videoUrl = cloudinaryService.uploadVideo(file);
//        Lesson lesson = lessonService.findLessonById(id);
//
//        lesson.setVideoUrl(videoUrl);
//        lessonService.saveLesson(lesson);
//        return ResponseEntity.ok(
//                Map.of(
//                        "message","Upload video success",
//                        "videoUrl",videoUrl
//                )
//        );
//    }
}

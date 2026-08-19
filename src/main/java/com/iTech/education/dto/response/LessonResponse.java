package com.iTech.education.dto.response;

import com.iTech.education.entity.Lesson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String videoUrl;
    private String content;
    private Integer orderIndex;
    private Integer duration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean locked;

    public static LessonResponse fromEntity(Lesson lesson) {
        return fromEntity(lesson, false);
    }

    public static LessonResponse fromEntityPreview(Lesson lesson) {
        return fromEntity(lesson, true);
    }

    private static LessonResponse fromEntity(Lesson lesson, boolean locked) {
        LessonResponse response = new LessonResponse();
        response.setId(lesson.getId());
        response.setCourseId(lesson.getCourse() != null ? lesson.getCourse().getId() : null);
        response.setTitle(lesson.getTitle());
        response.setOrderIndex(lesson.getOrderIndex());
        response.setDuration(lesson.getDuration());
        response.setCreatedAt(lesson.getCreatedAt());
        response.setUpdatedAt(lesson.getUpdatedAt());
        response.setLocked(locked);

        if (!locked) {
            response.setVideoUrl(lesson.getVideoUrl());
            response.setContent(lesson.getContent());
        }

        return response;
    }
}
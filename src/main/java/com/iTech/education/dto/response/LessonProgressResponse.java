package com.iTech.education.dto.response;

import com.iTech.education.entity.LessonProgress;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LessonProgressResponse {

    private Long lessonId;
    private Boolean isCompleted;
    private Integer watchedDuration;
    private LocalDateTime completedAt;
    private Double courseProgressPercent;

    public static LessonProgressResponse fromEntity(LessonProgress progress, Double courseProgressPercent) {
        LessonProgressResponse response = new LessonProgressResponse();
        response.setLessonId(progress.getLesson().getId());
        response.setIsCompleted(progress.getIsCompleted());
        response.setWatchedDuration(progress.getWatchedDuration());
        response.setCompletedAt(progress.getCompletedAt());
        response.setCourseProgressPercent(courseProgressPercent);
        return response;
    }
}

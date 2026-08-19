package com.iTech.education.repository;

import com.iTech.education.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    long countByUserIdAndLesson_CourseIdAndIsCompletedTrue(Long userId, Long courseId);

    List<LessonProgress> findByUserIdAndLesson_CourseId(Long userId, Long courseId);
}

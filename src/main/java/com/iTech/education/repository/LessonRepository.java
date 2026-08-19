package com.iTech.education.repository;

import com.iTech.education.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    @Query("SELECT l FROM Lesson l JOIN FETCH l.course WHERE l.id = :id")
    Optional<Lesson> findByIdWithCourse(@Param("id") Long id);

    long countByCourseId(Long courseId);
}

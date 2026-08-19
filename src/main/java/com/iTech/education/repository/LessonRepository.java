package com.iTech.education.repository;

import com.iTech.education.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository  extends JpaRepository<Lesson,Long> {
    // Danh sách bài học thuộc 1 course, sắp xếp đúng thứ tự học (order_index tăng dần)
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    long countByCourseId(Long courseId);
}

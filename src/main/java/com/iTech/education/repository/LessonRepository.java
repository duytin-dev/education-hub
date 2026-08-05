package com.iTech.education.repository;

import com.iTech.education.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository  extends JpaRepository<Lesson,Long> {
}

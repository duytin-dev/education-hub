package com.iTech.education.service.impl;

import com.iTech.education.entity.Lesson;
import com.iTech.education.repository.LessonRepository;
import com.iTech.education.service.LessonService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    public LessonServiceImpl(LessonRepository lessonRepository){
        this.lessonRepository = lessonRepository;
    }
    @Override
    public Lesson findLessonById(Long idLesson) {
        Optional<Lesson> lesson = lessonRepository.findById(idLesson);
        return lesson.orElse(null);

    }
}

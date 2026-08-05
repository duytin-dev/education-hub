package com.iTech.education.service.impl;

import com.iTech.education.repository.CourseRepository;
import com.iTech.education.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    public CourseServiceImpl(CourseRepository courseRepository){
        this.courseRepository = courseRepository;
    }
}

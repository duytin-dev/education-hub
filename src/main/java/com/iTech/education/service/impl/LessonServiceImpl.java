package com.iTech.education.service.impl;

import com.iTech.education.dto.request.LessonRequest;
import com.iTech.education.dto.response.LessonResponse;
import com.iTech.education.entity.Course;
import com.iTech.education.entity.Lesson;
import com.iTech.education.entity.User;
import com.iTech.education.exception.ResourceNotFoundException;
import com.iTech.education.repository.CourseRepository;
import com.iTech.education.repository.LessonRepository;
import com.iTech.education.service.CloudinaryService;
import com.iTech.education.service.LessonService;
import com.iTech.education.service.UserService;
import com.iTech.education.utils.RoleType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    public LessonServiceImpl(LessonRepository lessonRepository,
                             CourseRepository courseRepository,
                             UserService userService,
                             CloudinaryService cloudinaryService) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public LessonResponse create(LessonRequest request, String currentUserEmail) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy khóa học với id: " + request.getCourseId()));

        checkOwnership(course, currentUserEmail);

        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setDuration(request.getDuration());

        Lesson saved = lessonRepository.save(lesson);
        return LessonResponse.fromEntity(saved);
    }

    @Override
    public LessonResponse update(Long id, LessonRequest request, String currentUserEmail) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với id: " + id));

        checkOwnership(lesson.getCourse(), currentUserEmail);

        // cho phép đổi lesson sang course khác nếu courseId trong request khác course hiện tại
        if (!lesson.getCourse().getId().equals(request.getCourseId())) {
            Course newCourse = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy khóa học với id: " + request.getCourseId()));
            checkOwnership(newCourse, currentUserEmail);
            lesson.setCourse(newCourse);
        }

        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setDuration(request.getDuration());

        Lesson updated = lessonRepository.save(lesson);
        return LessonResponse.fromEntity(updated);
    }

    @Override
    public void delete(Long id, String currentUserEmail) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với id: " + id));

        checkOwnership(lesson.getCourse(), currentUserEmail);
        lessonRepository.delete(lesson);
    }

    @Override
    public LessonResponse getById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với id: " + id));
        return LessonResponse.fromEntity(lesson);
    }

    @Override
    public List<LessonResponse> getByCourseId(Long courseId) {
        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .map(LessonResponse::fromEntity)
                .toList();
    }

    @Override
    public LessonResponse uploadVideo(Long id, MultipartFile file, String currentUserEmail) throws IOException {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với id: " + id));

        checkOwnership(lesson.getCourse(), currentUserEmail);

        String videoUrl = cloudinaryService.uploadVideo(file);
        lesson.setVideoUrl(videoUrl);

        Lesson updated = lessonRepository.save(lesson);
        return LessonResponse.fromEntity(updated);
    }

    /**
     * Lesson không có instructor riêng - phải lấy qua course.getInstructor()
     * để biết ai được phép sửa/xóa lesson đó.
     */
    private void checkOwnership(Course course, String currentUserEmail) {
        User currentUser = userService.handleGetUserByUsername(currentUserEmail);

        boolean isAdmin = currentUser.getRole() == RoleType.ADMIN;
        boolean isOwner = course.getInstructor().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Bạn không có quyền thao tác trên bài học của khóa học này");
        }
    }
}

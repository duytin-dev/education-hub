package com.iTech.education.service.impl;

import com.iTech.education.dto.request.LessonRequest;
import com.iTech.education.dto.request.ProgressUpdateRequest;
import com.iTech.education.dto.response.LessonProgressResponse;
import com.iTech.education.dto.response.LessonResponse;
import com.iTech.education.entity.Course;
import com.iTech.education.entity.Lesson;
import com.iTech.education.entity.User;
import com.iTech.education.exception.ResourceNotFoundException;
import com.iTech.education.repository.CourseRepository;
import com.iTech.education.repository.EnrollmentRepository;
import com.iTech.education.repository.LessonRepository;
import com.iTech.education.service.CloudinaryService;
import com.iTech.education.service.LessonService;
import com.iTech.education.service.ProgressService;
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
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final ProgressService progressService;

    public LessonServiceImpl(LessonRepository lessonRepository,
                             CourseRepository courseRepository,
                             EnrollmentRepository enrollmentRepository,
                             UserService userService,
                             CloudinaryService cloudinaryService,
                             ProgressService progressService) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
        this.progressService = progressService;
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
    public LessonResponse getById(Long id, String currentUserEmail) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với id: " + id));

        if (canAccessFullContent(lesson.getCourse(), currentUserEmail)) {
            return LessonResponse.fromEntity(lesson);
        }

        return LessonResponse.fromEntityPreview(lesson);
    }

    @Override
    public List<LessonResponse> getByCourseId(Long courseId, String currentUserEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với id: " + courseId));

        boolean fullAccess = canAccessFullContent(course, currentUserEmail);

        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .map(lesson -> fullAccess ? LessonResponse.fromEntity(lesson) : LessonResponse.fromEntityPreview(lesson))
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

    @Override
    public LessonProgressResponse updateProgress(Long lessonId,
                                                 ProgressUpdateRequest request,
                                                 String currentUserEmail) {
        return progressService.updateLessonProgress(lessonId, request, currentUserEmail);
    }

    private boolean canAccessFullContent(Course course, String currentUserEmail) {
        if (currentUserEmail == null) {
            return false;
        }

        User currentUser = userService.handleGetUserByUsername(currentUserEmail);

        if (currentUser.getRole() == RoleType.ADMIN) {
            return true;
        }

        if (course.getInstructor().getId().equals(currentUser.getId())) {
            return true;
        }

        return enrollmentRepository.existsByUserIdAndCourseId(currentUser.getId(), course.getId());
    }

    private void checkOwnership(Course course, String currentUserEmail) {
        User currentUser = userService.handleGetUserByUsername(currentUserEmail);

        boolean isAdmin = currentUser.getRole() == RoleType.ADMIN;
        boolean isOwner = course.getInstructor().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Bạn không có quyền thao tác trên bài học của khóa học này");
        }
    }
}

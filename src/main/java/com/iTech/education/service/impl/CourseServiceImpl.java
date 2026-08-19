package com.iTech.education.service.impl;

import com.iTech.education.dto.request.CourseRequest;
import com.iTech.education.dto.response.CourseResponse;
import com.iTech.education.entity.Category;
import com.iTech.education.entity.Course;
import com.iTech.education.entity.User;
import com.iTech.education.exception.ResourceNotFoundException;
import com.iTech.education.repository.CategoryRepository;
import com.iTech.education.repository.CourseRepository;
import com.iTech.education.service.CourseService;
import com.iTech.education.service.UserService;
import com.iTech.education.specification.CourseSpecificationImpl;
import com.iTech.education.utils.CourseStatus;
import com.iTech.education.utils.Level;
import com.iTech.education.utils.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public CourseServiceImpl(CourseRepository courseRepository,
                             CategoryRepository categoryRepository,
                             UserService userService) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    @Override
    public CourseResponse create(CourseRequest request, String currentUserEmail) {
        User instructor = userService.handleGetUserByUsername(currentUserEmail);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy danh mục với id: " + request.getCategoryId()));

        Optional<Course> existingCourse = courseRepository.findByTitle(request.getTitle());
        if (existingCourse.isPresent()) {
            throw new IllegalArgumentException("Tiêu đề khóa học đã tồn tại. Vui lòng chọn tiêu đề khác.");
        }

        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setThumbnail(request.getThumbnail());
        course.setPrice(request.getPrice());
        course.setLevel(request.getLevel());
        course.setStatus(CourseStatus.DRAFT); // luôn tạo ở trạng thái DRAFT, không tin client tự set PUBLISHED
        course.setCategory(category);
        course.setInstructor(instructor); // instructor luôn là người đang login, KHÔNG lấy từ request

        Course saved = courseRepository.save(course);
        return CourseResponse.fromEntity(saved);
    }

    @Override
    public CourseResponse update(Long id, CourseRequest request, String currentUserEmail) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với id: " + id));

        checkOwnership(course, currentUserEmail);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy danh mục với id: " + request.getCategoryId()));

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setThumbnail(request.getThumbnail());
        course.setPrice(request.getPrice());
        course.setLevel(request.getLevel());
        course.setCategory(category);
        // không đổi status, instructor ở đây - dùng API riêng (changeStatus) cho việc đổi trạng thái

        Course updated = courseRepository.save(course);
        return CourseResponse.fromEntity(updated);
    }

    @Override
    public void delete(Long id, String currentUserEmail) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với id: " + id));

        checkOwnership(course, currentUserEmail);
        courseRepository.delete(course);
    }

    @Override
    public CourseResponse getById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với id: " + id));
        return CourseResponse.fromEntity(course);
    }

    @Override
    public Page<CourseResponse> search(String keyword, Long categoryId, Level level,
                                       BigDecimal minPrice, BigDecimal maxPrice,
                                       CourseStatus status, Long instructorId,
                                       String currentUserEmail, Pageable pageable) {
        Long scopedInstructorId = instructorId;
        if (currentUserEmail != null && !currentUserEmail.isBlank()
                && !"anonymousUser".equals(currentUserEmail)) {
            User currentUser = userService.handleGetUserByUsername(currentUserEmail);
            if (currentUser.getRole() == RoleType.INSTRUCTOR) {
                scopedInstructorId = currentUser.getId();
            }
        }

        var spec = CourseSpecificationImpl.filter(
                keyword, categoryId, level, minPrice, maxPrice, status, scopedInstructorId);
        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        return coursePage.map(CourseResponse::fromEntity);
    }

    @Override
    public CourseResponse changeStatus(Long id, CourseStatus newStatus, String currentUserEmail) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với id: " + id));

        checkOwnership(course, currentUserEmail);

        course.setStatus(newStatus);
        Course updated = courseRepository.save(course);
        return CourseResponse.fromEntity(updated);
    }

    /**
     * Chỉ ADMIN hoặc đúng instructor sở hữu course mới được sửa/xóa.
     * @PreAuthorize chỉ check được ROLE, không check được "có phải chủ course không"
     * nên phần này bắt buộc phải viết tay trong Service.
     */
    private void checkOwnership(Course course, String currentUserEmail) {
        User currentUser = userService.handleGetUserByUsername(currentUserEmail);

        boolean isAdmin = currentUser.getRole() == RoleType.ADMIN;
        boolean isOwner = course.getInstructor().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Bạn không có quyền thao tác trên khóa học này");
        }
    }
}

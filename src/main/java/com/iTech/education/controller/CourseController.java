package com.iTech.education.controller;

import com.iTech.education.dto.request.CourseRequest;
import com.iTech.education.dto.response.ApiResponse;
import com.iTech.education.dto.response.CourseResponse;
import com.iTech.education.service.CourseService;
import com.iTech.education.utils.CourseStatus;
import com.iTech.education.utils.Level;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/courses")
@Validated
public class CourseController {
    private final CourseService courseService;
    public CourseController(CourseService courseService){
        this.courseService = courseService;
    }
    /**
     * GET /api/v1/courses?keyword=spring&categoryId=1&level=BEGINNER&minPrice=100000&maxPrice=600000&page=0&size=10
     * Tất cả param đều optional. Public - ai cũng xem được, kể cả chưa login.
     * INSTRUCTOR luôn chỉ nhận khóa của chính mình (lọc instructor_id trên DB).
     */
    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Level level,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication authentication
    ) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        String email = emailOf(authentication);

        Page<CourseResponse> result = courseService.search(
                keyword, categoryId, level, minPrice, maxPrice, status, instructorId, email, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private String emailOf(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String name = authentication.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) {
            return null;
        }
        return name;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        CourseResponse course = courseService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> create(@Valid @RequestBody CourseRequest request, Authentication authentication) {
        CourseResponse created = courseService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo khóa học thành công", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody CourseRequest request,
                                    Authentication authentication) {
        CourseResponse updated = courseService.update(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khóa học thành công", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        courseService.delete(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Xóa khóa học thành công", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestParam CourseStatus status,
                                          Authentication authentication) {
        CourseResponse updated = courseService.changeStatus(id, status, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", updated));
    }
}

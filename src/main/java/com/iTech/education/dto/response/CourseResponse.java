package com.iTech.education.dto.response;

import com.iTech.education.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private String thumbnail;
    private BigDecimal price;
    private String level;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // chỉ lấy thông tin rút gọn, không trả cả object Category/User đầy đủ
    private Long categoryId;
    private String categoryName;
    private Long instructorId;
    private String instructorName;

    public static CourseResponse fromEntity(Course course) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setThumbnail(course.getThumbnail());
        response.setPrice(course.getPrice());
        response.setLevel(course.getLevel() != null ? course.getLevel().name() : null);
        response.setStatus(course.getStatus() != null ? course.getStatus().name() : null);
        response.setCreatedAt(course.getCreatedAt());
        response.setUpdatedAt(course.getUpdatedAt());

        if (course.getCategory() != null) {
            response.setCategoryId(course.getCategory().getId());
            response.setCategoryName(course.getCategory().getName());
        }
        if (course.getInstructor() != null) {
            response.setInstructorId(course.getInstructor().getId());
            response.setInstructorName(course.getInstructor().getFullName());
        }

        return response;
    }
}
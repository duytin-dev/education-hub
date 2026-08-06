package com.iTech.education.dto.request;

import com.iTech.education.utils.CourseStatus;
import com.iTech.education.utils.Level;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CourseRequest {

    @NotBlank(message = "Tiêu đề khóa học không được để trống")
    @Size(min = 5, max = 200, message = "Tiêu đề phải từ 5 đến 200 ký tự")
    private String title;

    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    private String description;

    private String thumbnail;

    @NotNull(message = "Giá khóa học không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá không được âm")
    private BigDecimal price;

    @NotNull(message = "Level không được để trống")
    private Level level;
    // Không cho client tự set status lúc tạo — service sẽ mặc định DRAFT.
    // Chỉ dùng field này khi gọi API đổi trạng thái riêng (PATCH /status).
    private CourseStatus status;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;
}
package com.iTech.education.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonRequest {

    @NotNull(message = "Khóa học không được để trống")
    private Long courseId;

    @NotBlank(message = "Tiêu đề bài học không được để trống")
    @Size(min = 3, max = 200, message = "Tiêu đề phải từ 3 đến 200 ký tự")
    private String title;

    private String content;

    @Min(value = 1, message = "Thứ tự bài học phải lớn hơn 0")
    private Integer orderIndex;

    @Min(value = 0, message = "Thời lượng không được âm")
    private Integer duration; // đơn vị: giây

    // videoUrl KHÔNG có ở đây - video upload qua API riêng PUT /lessons/{id}/video
    // vì upload file và tạo thông tin lesson là 2 hành động tách biệt
}
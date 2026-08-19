package com.iTech.education.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreatePaymentRequest {

    @NotEmpty(message = "Danh sách khóa học không được để trống")
    private List<Long> courseIds;
}

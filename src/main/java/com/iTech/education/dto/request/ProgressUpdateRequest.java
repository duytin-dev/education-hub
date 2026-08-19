package com.iTech.education.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgressUpdateRequest {

    private Boolean isCompleted;

    @Min(value = 0, message = "Thời lượng xem không được âm")
    private Integer watchedDuration;
}

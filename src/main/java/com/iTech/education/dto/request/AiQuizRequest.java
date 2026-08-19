package com.iTech.education.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiQuizRequest {
    @NotNull
    private Long lessonId;
    @Min(3)
    @Max(10)
    private Integer count = 5;
}

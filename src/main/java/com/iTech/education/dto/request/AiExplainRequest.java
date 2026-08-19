package com.iTech.education.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiExplainRequest {
    @NotNull
    private Long lessonId;
    @NotBlank
    private String question;
}

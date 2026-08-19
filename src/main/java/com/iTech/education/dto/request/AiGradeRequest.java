package com.iTech.education.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiGradeRequest {
    @NotNull
    private Long lessonId;
    @NotEmpty
    private List<AiQuizQuestionPayload> questions;
    @NotEmpty
    private List<AiGradeAnswerPayload> answers;
}

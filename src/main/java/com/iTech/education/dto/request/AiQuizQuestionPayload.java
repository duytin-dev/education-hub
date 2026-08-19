package com.iTech.education.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiQuizQuestionPayload {
    private Integer id;
    private String question;
    private List<String> options;
    private Integer correctIndex;
    private String explain;
}

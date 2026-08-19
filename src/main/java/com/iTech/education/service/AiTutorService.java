package com.iTech.education.service;

import com.iTech.education.dto.request.AiExplainRequest;
import com.iTech.education.dto.request.AiGradeRequest;
import com.iTech.education.dto.request.AiQuizRequest;

import java.util.Map;

public interface AiTutorService {
    Map<String, Object> explain(AiExplainRequest request, String currentUserEmail);

    Map<String, Object> quiz(AiQuizRequest request, String currentUserEmail);

    Map<String, Object> grade(AiGradeRequest request, String currentUserEmail);
}

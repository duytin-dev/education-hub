package com.iTech.education.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iTech.education.dto.request.AiExplainRequest;
import com.iTech.education.dto.request.AiGradeAnswerPayload;
import com.iTech.education.dto.request.AiGradeRequest;
import com.iTech.education.dto.request.AiQuizQuestionPayload;
import com.iTech.education.dto.request.AiQuizRequest;
import com.iTech.education.entity.Course;
import com.iTech.education.entity.Lesson;
import com.iTech.education.entity.User;
import com.iTech.education.exception.ResourceNotFoundException;
import com.iTech.education.repository.EnrollmentRepository;
import com.iTech.education.repository.LessonRepository;
import com.iTech.education.repository.UserRepository;
import com.iTech.education.service.AiTutorService;
import com.iTech.education.utils.RoleType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiTutorServiceImpl implements AiTutorService {

    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final String aiBaseUrl;
    private final int aiReadTimeoutMs;

    public AiTutorServiceImpl(LessonRepository lessonRepository,
                              EnrollmentRepository enrollmentRepository,
                              UserRepository userRepository,
                              ObjectMapper objectMapper,
                              @Value("${app.ai.base-url:http://127.0.0.1:8000}") String aiBaseUrl,
                              @Value("${app.ai.read-timeout-ms:180000}") int aiReadTimeoutMs) {
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.aiBaseUrl = aiBaseUrl.endsWith("/") ? aiBaseUrl.substring(0, aiBaseUrl.length() - 1) : aiBaseUrl;
        this.aiReadTimeoutMs = aiReadTimeoutMs;
    }

    @Override
    public Map<String, Object> explain(AiExplainRequest request, String currentUserEmail) {
        LessonContext ctx = loadContext(request.getLessonId(), currentUserEmail);
        Map<String, Object> body = contextBody(ctx);
        body.put("question", request.getQuestion());
        return post("/explain", body);
    }

    @Override
    public Map<String, Object> quiz(AiQuizRequest request, String currentUserEmail) {
        LessonContext ctx = loadContext(request.getLessonId(), currentUserEmail);
        Map<String, Object> body = contextBody(ctx);
        body.put("count", request.getCount() == null ? 5 : request.getCount());
        return post("/quiz", body);
    }

    @Override
    public Map<String, Object> grade(AiGradeRequest request, String currentUserEmail) {
        LessonContext ctx = loadContext(request.getLessonId(), currentUserEmail);
        Map<String, Object> body = contextBody(ctx);
        body.put("questions", request.getQuestions().stream().map(this::questionMap).toList());
        body.put("answers", request.getAnswers().stream().map(this::answerMap).toList());
        return post("/grade", body);
    }

    private Map<String, Object> questionMap(AiQuizQuestionPayload q) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", q.getId());
        map.put("question", q.getQuestion());
        map.put("options", q.getOptions());
        map.put("correctIndex", q.getCorrectIndex());
        map.put("explain", q.getExplain());
        return map;
    }

    private Map<String, Object> answerMap(AiGradeAnswerPayload a) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", a.getId());
        map.put("selectedIndex", a.getSelectedIndex());
        return map;
    }

    private LessonContext loadContext(Long lessonId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        if (user.getRole() != RoleType.STUDENT) {
            throw new AccessDeniedException("Chỉ học viên mới dùng gia sư AI");
        }
        Lesson lesson = lessonRepository.findByIdWithCourse(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học"));
        Course course = lesson.getCourse();
        if (!enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
            throw new AccessDeniedException("Bạn cần ghi danh khóa học để dùng gia sư AI");
        }
        return new LessonContext(
                course.getTitle(),
                lesson.getTitle(),
                lesson.getContent() == null ? "" : lesson.getContent(),
                course.getLevel() == null ? "BEGINNER" : course.getLevel().name()
        );
    }

    private Map<String, Object> contextBody(LessonContext ctx) {
        Map<String, Object> body = new HashMap<>();
        body.put("course_title", ctx.courseTitle());
        body.put("lesson_title", ctx.lessonTitle());
        body.put("lesson_content", ctx.lessonContent());
        body.put("level", ctx.level());
        return body;
    }

    private Map<String, Object> post(String path, Map<String, Object> body) {
        HttpURLConnection conn = null;
        try {
            byte[] json = objectMapper.writeValueAsBytes(body);
            conn = (HttpURLConnection) URI.create(aiBaseUrl + path).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(aiReadTimeoutMs);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setFixedLengthStreamingMode(json.length);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json);
            }
            int status = conn.getResponseCode();
            InputStream stream = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String response = stream == null ? "" : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            if (status >= 400) {
                throw new IllegalStateException("Dịch vụ AI " + path + " trả " + status + ": " + response);
            }
            if (response.isBlank()) {
                return Map.of();
            }
            return objectMapper.readValue(response, new TypeReference<>() {});
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Không gọi được dịch vụ AI tại " + path + ": " + ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private record LessonContext(String courseTitle, String lessonTitle, String lessonContent, String level) {}
}

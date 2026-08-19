package com.iTech.education;

import com.iTech.education.entity.Category;
import com.iTech.education.entity.Course;
import com.iTech.education.entity.User;
import com.iTech.education.repository.CategoryRepository;
import com.iTech.education.repository.CourseRepository;
import com.iTech.education.repository.UserRepository;
import com.iTech.education.security.JwtTokenProvider;
import com.iTech.education.support.TestJwtSupport;
import com.iTech.education.utils.CourseStatus;
import com.iTech.education.utils.Level;
import com.iTech.education.utils.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String instructorToken;
    private String otherInstructorToken;
    private String studentToken;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setName("Backend");
        category.setDescription("Backend courses");
        categoryId = categoryRepository.save(category).getId();

        User instructor = saveUser("instructor@test.com", RoleType.INSTRUCTOR);
        User otherInstructor = saveUser("other-instructor@test.com", RoleType.INSTRUCTOR);
        User student = saveUser("student-course@test.com", RoleType.STUDENT);

        instructorToken = TestJwtSupport.bearerToken(jwtTokenProvider, instructor);
        otherInstructorToken = TestJwtSupport.bearerToken(jwtTokenProvider, otherInstructor);
        studentToken = TestJwtSupport.bearerToken(jwtTokenProvider, student);
    }

    @Test
    void getCourse_shouldBePublic_withoutAuthentication() throws Exception {
        Course course = saveCourse("Public Course", instructorEmail("instructor@test.com"));

        mockMvc.perform(get("/api/v1/courses/{id}", course.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Public Course"));
    }

    @Test
    void createCourse_shouldReturn201_whenUserIsInstructor() throws Exception {
        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCourseJson("Spring Boot Basics")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void createCourse_shouldReturn403_whenUserIsStudent() throws Exception {
        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCourseJson("Student Course")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCourse_shouldReturn200_whenOwnerUpdates() throws Exception {
        Course course = saveCourse("Owner Course", instructorEmail("instructor@test.com"));

        mockMvc.perform(put("/api/v1/courses/{id}", course.getId())
                        .header("Authorization", instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCourseJson("Updated Owner Course")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Owner Course"));
    }

    @Test
    void updateCourse_shouldReturn403_whenNonOwnerUpdates() throws Exception {
        Course course = saveCourse("Protected Course", instructorEmail("instructor@test.com"));

        mockMvc.perform(put("/api/v1/courses/{id}", course.getId())
                        .header("Authorization", otherInstructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCourseJson("Hacked Course")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteCourse_shouldReturn403_whenNonOwnerDeletes() throws Exception {
        Course course = saveCourse("Delete Protected", instructorEmail("instructor@test.com"));

        mockMvc.perform(delete("/api/v1/courses/{id}", course.getId())
                        .header("Authorization", otherInstructorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchCourses_shouldReturnPagedResults() throws Exception {
        saveCourse("Java Course", instructorEmail("instructor@test.com"));

        mockMvc.perform(get("/api/v1/courses")
                        .param("keyword", "Java")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Java Course"));
    }

    private User saveUser(String email, RoleType role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("pass123"));
        user.setFullName(role.name() + " User");
        user.setPhone("0123456789");
        user.setRole(role);
        user.setIsActive(true);
        return userRepository.save(user);
    }

    private Course saveCourse(String title, User instructor) {
        Category category = categoryRepository.findById(categoryId).orElseThrow();

        Course course = new Course();
        course.setTitle(title);
        course.setDescription("Course description");
        course.setPrice(BigDecimal.valueOf(100000));
        course.setLevel(Level.BEGINNER);
        course.setStatus(CourseStatus.PUBLISHED);
        course.setCategory(category);
        course.setInstructor(instructor);
        return courseRepository.save(course);
    }

    private User instructorEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    private String validCourseJson(String title) throws Exception {
        String template = """
                {
                  "title": "%s",
                  "description": "Course description",
                  "price": 100000,
                  "level": "BEGINNER",
                  "categoryId": %d
                }
                """;
        return template.formatted(title, categoryId);
    }
}

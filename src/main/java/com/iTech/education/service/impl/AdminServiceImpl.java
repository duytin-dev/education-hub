package com.iTech.education.service.impl;

import com.iTech.education.dto.response.AdminDashboardResponse;
import com.iTech.education.dto.response.CourseResponse;
import com.iTech.education.dto.response.MonthlyPoint;
import com.iTech.education.dto.response.NamedCount;
import com.iTech.education.dto.response.UserResponse;
import com.iTech.education.entity.User;
import com.iTech.education.exception.ResourceNotFoundException;
import com.iTech.education.repository.CategoryRepository;
import com.iTech.education.repository.CommentRepository;
import com.iTech.education.repository.CourseRepository;
import com.iTech.education.repository.EnrollmentRepository;
import com.iTech.education.repository.LessonRepository;
import com.iTech.education.repository.TransactionRepository;
import com.iTech.education.repository.UserRepository;
import com.iTech.education.service.AdminService;
import com.iTech.education.utils.CourseStatus;
import com.iTech.education.utils.RoleType;
import com.iTech.education.utils.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MM/yyyy");

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final TransactionRepository transactionRepository;

    public AdminServiceImpl(UserRepository userRepository,
                            CourseRepository courseRepository,
                            LessonRepository lessonRepository,
                            EnrollmentRepository enrollmentRepository,
                            CategoryRepository categoryRepository,
                            CommentRepository commentRepository,
                            TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.categoryRepository = categoryRepository;
        this.commentRepository = commentRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public AdminDashboardResponse getDashboard() {
        AdminDashboardResponse dashboard = new AdminDashboardResponse();
        LocalDateTime from = YearMonth.now().minusMonths(5).atDay(1).atStartOfDay();

        dashboard.setTotalUsers(userRepository.count());
        dashboard.setTotalStudents(userRepository.countByRole(RoleType.STUDENT));
        dashboard.setTotalInstructors(userRepository.countByRole(RoleType.INSTRUCTOR));
        dashboard.setTotalAdmins(userRepository.countByRole(RoleType.ADMIN));
        dashboard.setActiveUsers(userRepository.countByIsActive(true));
        dashboard.setInactiveUsers(userRepository.countByIsActive(false));

        dashboard.setTotalCourses(courseRepository.count());
        dashboard.setPublishedCourses(courseRepository.countByStatus(CourseStatus.PUBLISHED));
        dashboard.setDraftCourses(courseRepository.countByStatus(CourseStatus.DRAFT));
        dashboard.setArchivedCourses(courseRepository.countByStatus(CourseStatus.ARCHIVED));

        dashboard.setTotalLessons(lessonRepository.count());
        dashboard.setTotalEnrollments(enrollmentRepository.count());
        dashboard.setTotalCategories(categoryRepository.count());
        dashboard.setTotalComments(commentRepository.count());

        dashboard.setTotalTransactions(transactionRepository.count());
        dashboard.setSuccessfulTransactions(transactionRepository.countByStatus(TransactionStatus.SUCCESS));
        dashboard.setTotalRevenue(transactionRepository.sumAmountByStatus(TransactionStatus.SUCCESS));

        dashboard.setUsersByRole(toNamedCounts(userRepository.countGroupByRole()));
        dashboard.setCoursesByStatus(toNamedCounts(courseRepository.countGroupByStatus()));
        dashboard.setCoursesByLevel(toNamedCounts(courseRepository.countGroupByLevel()));
        dashboard.setCoursesByCategory(toNamedCounts(courseRepository.countGroupByCategory()));

        dashboard.setMonthlyUsers(fillMonthly(from, userRepository.countMonthlySince(from), false));
        dashboard.setMonthlyStudents(fillMonthly(from, userRepository.countMonthlyByRoleSince(from, RoleType.STUDENT), false));
        dashboard.setMonthlyEnrollments(fillMonthly(from, enrollmentRepository.countMonthlySince(from), false));
        dashboard.setMonthlyRevenue(fillMonthly(from, transactionRepository.sumMonthlySince(TransactionStatus.SUCCESS, from), true));
        dashboard.setTopCoursesByStudents(toTopCourses(enrollmentRepository.countTopCoursesByStudents(), 10));

        dashboard.setRecentUsers(
                userRepository.findAll(PageRequest.of(0, 5, Sort.by("id").descending()))
                        .map(UserResponse::fromEntity)
                        .getContent()
        );
        dashboard.setRecentCourses(
                courseRepository.findAll(PageRequest.of(0, 5, Sort.by("id").descending()))
                        .map(CourseResponse::fromEntity)
                        .getContent()
        );
        return dashboard;
    }

    @Override
    public Page<UserResponse> searchUsers(String keyword, RoleType role, Boolean isActive, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> cb.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("fullName")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("phone"), "")), like)
            ));
        }
        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        }

        return userRepository.findAll(spec, pageable).map(UserResponse::fromEntity);
    }

    @Override
    @Transactional
    public UserResponse changeRole(Long userId, RoleType role, String currentAdminEmail) {
        User user = getUser(userId);
        preventSelfLock(user, currentAdminEmail);

        if (user.getRole() == RoleType.ADMIN && role != RoleType.ADMIN
                && userRepository.countByRole(RoleType.ADMIN) <= 1) {
            throw new IllegalArgumentException("Không thể hạ quyền admin cuối cùng");
        }

        user.setRole(role);
        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse changeActive(Long userId, boolean isActive, String currentAdminEmail) {
        User user = getUser(userId);
        preventSelfLock(user, currentAdminEmail);

        if (user.getRole() == RoleType.ADMIN && !isActive
                && userRepository.countByRole(RoleType.ADMIN) <= 1) {
            throw new IllegalArgumentException("Không thể khóa tài khoản admin cuối cùng");
        }

        user.setIsActive(isActive);
        return UserResponse.fromEntity(userRepository.save(user));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với id: " + userId));
    }

    private void preventSelfLock(User target, String currentAdminEmail) {
        if (target.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new AccessDeniedException("Bạn không thể tự thay đổi quyền hoặc trạng thái của chính mình");
        }
    }

    private List<NamedCount> toTopCourses(List<Object[]> rows, int limit) {
        List<NamedCount> result = new ArrayList<>();
        int size = Math.min(limit, rows.size());
        for (int i = 0; i < size; i++) {
            Object[] row = rows.get(i);
            if (row[1] == null) {
                continue;
            }
            result.add(new NamedCount(String.valueOf(row[1]), ((Number) row[2]).longValue()));
        }
        return result;
    }

    private List<NamedCount> toNamedCounts(List<Object[]> rows) {
        List<NamedCount> result = new ArrayList<>();
        for (Object[] row : rows) {
            if (row[0] == null) {
                continue;
            }
            result.add(new NamedCount(String.valueOf(row[0]), ((Number) row[1]).longValue()));
        }
        return result;
    }

    private List<MonthlyPoint> fillMonthly(LocalDateTime from, List<Object[]> rows, boolean hasAmount) {
        Map<String, MonthlyPoint> map = new HashMap<>();
        for (Object[] row : rows) {
            if (row[0] == null || row[1] == null) {
                continue;
            }
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            YearMonth ym = YearMonth.of(year, month);
            long count = hasAmount && row.length > 3
                    ? ((Number) row[3]).longValue()
                    : ((Number) row[2]).longValue();
            BigDecimal amount = hasAmount
                    ? toBigDecimal(row[2])
                    : BigDecimal.ZERO;
            map.put(ym.format(MONTH_KEY), new MonthlyPoint(ym.format(MONTH_KEY), ym.format(MONTH_LABEL), count, amount));
        }

        List<MonthlyPoint> points = new ArrayList<>();
        YearMonth cursor = YearMonth.from(from);
        YearMonth end = YearMonth.now();
        while (!cursor.isAfter(end)) {
            String key = cursor.format(MONTH_KEY);
            MonthlyPoint existing = map.get(key);
            points.add(existing != null
                    ? existing
                    : new MonthlyPoint(key, cursor.format(MONTH_LABEL), 0, BigDecimal.ZERO));
            cursor = cursor.plusMonths(1);
        }
        return points;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }
}

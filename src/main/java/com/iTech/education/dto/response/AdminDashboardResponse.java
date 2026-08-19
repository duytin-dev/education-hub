package com.iTech.education.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AdminDashboardResponse {

    private long totalUsers;
    private long totalStudents;
    private long totalInstructors;
    private long totalAdmins;
    private long activeUsers;
    private long inactiveUsers;

    private long totalCourses;
    private long publishedCourses;
    private long draftCourses;
    private long archivedCourses;

    private long totalLessons;
    private long totalEnrollments;
    private long totalCategories;
    private long totalComments;

    private long totalTransactions;
    private long successfulTransactions;
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    private List<NamedCount> usersByRole = new ArrayList<>();
    private List<NamedCount> coursesByStatus = new ArrayList<>();
    private List<NamedCount> coursesByLevel = new ArrayList<>();
    private List<NamedCount> coursesByCategory = new ArrayList<>();

    private List<MonthlyPoint> monthlyUsers = new ArrayList<>();
    private List<MonthlyPoint> monthlyStudents = new ArrayList<>();
    private List<MonthlyPoint> monthlyEnrollments = new ArrayList<>();
    private List<MonthlyPoint> monthlyRevenue = new ArrayList<>();

    private List<NamedCount> topCoursesByStudents = new ArrayList<>();

    private List<UserResponse> recentUsers = new ArrayList<>();
    private List<CourseResponse> recentCourses = new ArrayList<>();
}

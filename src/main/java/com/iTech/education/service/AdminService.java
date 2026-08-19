package com.iTech.education.service;

import com.iTech.education.dto.response.AdminDashboardResponse;
import com.iTech.education.dto.response.UserResponse;
import com.iTech.education.utils.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    AdminDashboardResponse getDashboard();

    Page<UserResponse> searchUsers(String keyword, RoleType role, Boolean isActive, Pageable pageable);

    UserResponse changeRole(Long userId, RoleType role, String currentAdminEmail);

    UserResponse changeActive(Long userId, boolean isActive, String currentAdminEmail);
}

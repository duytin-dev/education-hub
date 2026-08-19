package com.iTech.education.service;

import com.iTech.education.dto.request.ChangePasswordRequest;
import com.iTech.education.dto.request.UpdateProfileRequest;
import com.iTech.education.dto.response.UserResponse;
import com.iTech.education.entity.User;

public interface UserService {

    User handleGetUserByUsername(String username);

    UserResponse getProfile(String email);

    UserResponse updateProfile(String email, UpdateProfileRequest request);

    void changePassword(String email, ChangePasswordRequest request);
}

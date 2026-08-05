package com.iTech.education.service;

import com.iTech.education.dto.request.ChangePasswordRequest;
import com.iTech.education.dto.request.LoginRequest;
import com.iTech.education.dto.request.RegisterRequest;
import com.iTech.education.dto.request.UpdateProfileRequest;
import com.iTech.education.dto.response.AuthResponse;
import com.iTech.education.dto.response.UserResponse;
import com.iTech.education.entity.User;

public interface UserService {

    User handleGetUserByUsername(String username);

//    AuthResponse register(RegisterRequest request);
//
//    AuthResponse login(LoginRequest request);
//
//    UserResponse getProfile(String email);
//
//    UserResponse updateProfile(String email, UpdateProfileRequest request);
//
//    void changePassword(String email, ChangePasswordRequest request);
}

package com.iTech.education.service.impl;

import com.iTech.education.dto.request.ChangePasswordRequest;
import com.iTech.education.dto.request.LoginRequest;
import com.iTech.education.dto.request.RegisterRequest;
import com.iTech.education.dto.request.UpdateProfileRequest;
import com.iTech.education.dto.response.AuthResponse;
import com.iTech.education.dto.response.UserResponse;
import com.iTech.education.entity.User;
import com.iTech.education.repository.UserRepository;
import com.iTech.education.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User handleGetUserByUsername(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));
    }




}

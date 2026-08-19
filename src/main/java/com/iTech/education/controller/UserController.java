package com.iTech.education.controller;

import com.iTech.education.dto.request.ChangePasswordRequest;
import com.iTech.education.dto.request.UpdateProfileRequest;
import com.iTech.education.dto.response.ApiResponse;
import com.iTech.education.dto.response.UserResponse;
import com.iTech.education.entity.User;
import com.iTech.education.exception.ResourceNotFoundException;
import com.iTech.education.repository.UserRepository;
import com.iTech.education.service.CloudinaryService;
import com.iTech.education.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@Validated
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(CloudinaryService cloudinaryService,
                          UserRepository userRepository,
                          UserService userService) {
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        UserResponse profile = userService.getProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                           Authentication authentication) {
        UserResponse updated = userService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ thành công", updated));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            Authentication authentication) {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }

    @PutMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file,
                                          Authentication authentication) throws IOException {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        String avatarUrl = cloudinaryService.uploadAvatar(file);
        user.setAvatar(avatarUrl);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Upload avatar thành công", Map.of("avatarUrl", avatarUrl)));
    }
}

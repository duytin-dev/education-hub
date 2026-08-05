package com.iTech.education.controller;

import com.iTech.education.entity.User;
import com.iTech.education.repository.UserRepository;
import com.iTech.education.service.CloudinaryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    public UserController(CloudinaryService cloudinaryService, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @PutMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        // yeu cau login
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")
                );
        String avatarUrl = cloudinaryService.uploadAvatar(file);
        user.setAvatar(avatarUrl);
        userRepository.save(user);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Upload avatar success",
                        "avatarUrl", avatarUrl
                )
        );
    }

}

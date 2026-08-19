package com.iTech.education.controller;

import com.iTech.education.dto.request.LoginRequest;
import com.iTech.education.dto.request.RegisterRequest;
import com.iTech.education.dto.request.ResendVerificationRequest;
import com.iTech.education.dto.response.ApiResponse;
import com.iTech.education.dto.response.AuthResponse;
import com.iTech.education.dto.response.UserResponse;
import com.iTech.education.entity.User;
import com.iTech.education.exception.EmailDuplicateException;
import com.iTech.education.repository.UserRepository;
import com.iTech.education.security.JwtTokenProvider;
import com.iTech.education.service.MailService;
import com.iTech.education.utils.RoleType;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final String frontendUrl;
    private final String backendUrl;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,
                          JwtTokenProvider jwtTokenProvider,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          MailService mailService,
                          @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl,
                          @Value("${app.backend-url:http://localhost:8080}") String backendUrl) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.frontendUrl = trimSlash(frontendUrl);
        this.backendUrl = trimSlash(backendUrl);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("invalid"));

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Vui lòng xác thực email trước khi đăng nhập. Kiểm tra hộp thư hoặc gửi lại email xác thực.");
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        String jwtToken = jwtTokenProvider.generateToken(authentication);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(jwtToken)
                .user(UserResponse.fromEntity(user))
                .build();

        return ResponseEntity.ok(ApiResponse.success("Login successfully", authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) throws EmailDuplicateException {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailDuplicateException("Email already exist !");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(RoleType.STUDENT);
        user.setIsActive(true);
        user.setEmailVerified(false);
        issueVerificationToken(user);
        userRepository.save(user);

        mailService.sendRegistrationSuccess(
                user.getEmail(),
                user.getFullName(),
                backendUrl + "/api/v1/verify-email?token=" + user.getVerificationToken()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.",
                        UserResponse.fromEntity(user)));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        String redirect = frontendUrl + "/login?verified=0";
        User user = userRepository.findByVerificationToken(token).orElse(null);
        if (user != null
                && user.getVerificationTokenExpiresAt() != null
                && user.getVerificationTokenExpiresAt().isAfter(LocalDateTime.now())) {
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            user.setVerificationTokenExpiresAt(null);
            userRepository.save(user);
            redirect = frontendUrl + "/login?verified=1";
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirect)).build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.iTech.education.exception.ResourceNotFoundException("Không tìm thấy tài khoản"));
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return ResponseEntity.ok(ApiResponse.success("Email đã được xác thực. Bạn có thể đăng nhập.", null));
        }
        issueVerificationToken(user);
        userRepository.save(user);
        mailService.sendRegistrationSuccess(
                user.getEmail(),
                user.getFullName(),
                backendUrl + "/api/v1/verify-email?token=" + user.getVerificationToken()
        );
        return ResponseEntity.ok(ApiResponse.success("Đã gửi lại email xác thực.", null));
    }

    private void issueVerificationToken(User user) {
        user.setVerificationToken(UUID.randomUUID().toString().replace("-", ""));
        user.setVerificationTokenExpiresAt(LocalDateTime.now().plusHours(24));
    }

    private String trimSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}

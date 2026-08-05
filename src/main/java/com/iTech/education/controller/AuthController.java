package com.iTech.education.controller;

import com.iTech.education.dto.request.LoginRequest;
import com.iTech.education.dto.request.RegisterRequest;
import com.iTech.education.dto.response.ApiResponse;
import com.iTech.education.dto.response.AuthResponse;
import com.iTech.education.dto.response.UserResponse;
import com.iTech.education.entity.User;
import com.iTech.education.exception.EmailDuplicateException;
import com.iTech.education.repository.UserRepository;
import com.iTech.education.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.iTech.education.utils.RoleType;
@RestController
@RequestMapping("/api/v1")
@Validated
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,JwtTokenProvider jwtTokenProvider,UserRepository userRepository,PasswordEncoder passwordEncoder
    ){
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository= userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        String jwtToken = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

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
        user.setRole(RoleType.STUDENT);   // <-- thêm dòng này
        user.setIsActive(true);

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Register successfully", UserResponse.fromEntity(user)));
    }
}

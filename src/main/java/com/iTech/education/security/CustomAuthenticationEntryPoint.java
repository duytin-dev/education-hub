package com.iTech.education.security;

import com.iTech.education.dto.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private  final ObjectMapper objectMapper ;
    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }
    // lỗi :Chưa đăng nhập hoặc JWT không hợp lệ -> ném ra AuthenticationException -> gọi hàm commence() -> trả về lỗi 401
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            ApiResponse<?> body = ApiResponse.error("Vui lòng đăng nhập để tiếp tục ");
            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }


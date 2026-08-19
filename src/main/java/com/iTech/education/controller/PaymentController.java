package com.iTech.education.controller;

import com.iTech.education.dto.request.CreatePaymentRequest;
import com.iTech.education.dto.response.ApiResponse;
import com.iTech.education.dto.response.PaymentResponse;
import com.iTech.education.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> createPayment(@Valid @RequestBody CreatePaymentRequest request,
                                           Authentication authentication,
                                           HttpServletRequest httpRequest) {
        PaymentResponse payment = paymentService.createPayment(
                request, authentication.getName(), clientIp(httpRequest));
        return ResponseEntity.ok(ApiResponse.success("Tạo giao dịch thành công", payment));
    }

    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> handleVnPayIpn(HttpServletRequest request) {
        Map<String, String> result = paymentService.handleVnPayIpn(queryParams(request));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vnpay/return")
    public ResponseEntity<Void> handleVnPayReturn(HttpServletRequest request) {
        String redirect = paymentService.handleVnPayReturn(queryParams(request));
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirect)).build();
    }

    private Map<String, String> queryParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

package com.iTech.education.service;

import com.iTech.education.dto.request.CreatePaymentRequest;
import com.iTech.education.dto.response.PaymentResponse;

import java.util.Map;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request, String currentUserEmail);

    Map<String, String> handleVnPayIpn(Map<String, String> params);
}

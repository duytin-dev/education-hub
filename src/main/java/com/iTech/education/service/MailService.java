package com.iTech.education.service;

import java.math.BigDecimal;
import java.util.List;

public interface MailService {

    void sendPaymentSuccess(String toEmail, String fullName, List<String> courseTitles,
                            BigDecimal totalAmount, Long firstCourseId);

    void sendRegistrationSuccess(String toEmail, String fullName, String verifyUrl);
}

package com.iTech.education.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {

    /** mock | vnpay */
    private String mode = "mock";

    private VnPay vnPay = new VnPay();

    @Getter
    @Setter
    public static class VnPay {
        private String tmnCode = "";
        private String hashSecret = "";
        private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        /** Backend nhận redirect từ VNPay, xác nhận giao dịch, rồi chuyển về frontend. */
        private String returnUrl = "http://localhost:8080/api/v1/payments/vnpay/return";
        private String frontendReturnUrl = "http://localhost:3000/payments/return";
        private String ipnUrl = "http://localhost:8080/api/v1/payments/vnpay/ipn";
    }
}

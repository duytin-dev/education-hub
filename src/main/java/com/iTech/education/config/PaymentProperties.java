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
        private String returnUrl = "http://localhost:5173/payments/return";
        private String ipnUrl = "http://localhost:8080/api/v1/payments/vnpay/ipn";
    }
}

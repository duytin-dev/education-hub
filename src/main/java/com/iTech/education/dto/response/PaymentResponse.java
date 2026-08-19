package com.iTech.education.dto.response;

import com.iTech.education.utils.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentResponse {

    private Long transactionId;
    private String transactionCode;
    private BigDecimal totalAmount;
    private TransactionStatus status;
    private String paymentUrl;
}

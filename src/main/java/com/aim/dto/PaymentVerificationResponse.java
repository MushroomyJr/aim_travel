package com.aim.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationResponse {
    private String type;
    private String orderId;
    private String sessionId;
    private Integer amount;
    private String status;
} 
package com.aim.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderPaymentRequest {
    private String sessionId;
    private String paymentStatus; // "paid", "failed", "cancelled"
    private String stripePaymentIntentId; // Optional: Stripe payment intent ID
} 
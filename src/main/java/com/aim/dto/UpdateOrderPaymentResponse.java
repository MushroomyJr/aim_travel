package com.aim.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderPaymentResponse {
    private Long orderId;
    private String orderNumber;
    private String itineraryNumber;
    private String passengerName;
    private String email;
    private String origin;
    private String destination;
    private String airline;
    private String cost;
    private String paymentStatus;
    private String sessionId;
    private String message;
    private boolean success;
} 
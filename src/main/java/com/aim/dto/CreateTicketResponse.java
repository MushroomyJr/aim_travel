package com.aim.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTicketResponse {
    private Long ticketId;
    private String orderNumber;
    private String passengerName;
    private String email;
    private String origin;
    private String destination;
    private String airline;
    private String cost;
    private String paymentSessionUrl; // Stripe checkout URL
    private String paymentSessionId; // Stripe session ID
    private String status; // "pending_payment", "paid", "cancelled"
} 
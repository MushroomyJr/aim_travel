package com.aim.service;

import com.aim.dto.CreateCheckoutSessionRequest;
import com.aim.dto.CheckoutSessionResponse;
import com.aim.dto.PaymentVerificationResponse;
import com.aim.model.Order;

public interface PaymentService {
    CheckoutSessionResponse createPaymentSession(Order order, String userEmail);
    PaymentVerificationResponse processPaymentSuccess(String sessionId);
    void updateOrderPaymentStatus(Order order, String paymentStatus, String sessionId);
} 
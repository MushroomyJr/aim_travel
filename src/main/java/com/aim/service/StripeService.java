package com.aim.service;

import com.aim.dto.CreateCheckoutSessionRequest;
import com.aim.dto.CheckoutSessionResponse;
import com.aim.dto.PaymentVerificationResponse;

public interface StripeService {
    CheckoutSessionResponse createCheckoutSession(CreateCheckoutSessionRequest request);
    PaymentVerificationResponse verifyPayment(String sessionId);
} 
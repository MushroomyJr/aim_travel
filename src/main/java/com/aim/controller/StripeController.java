package com.aim.controller;

import com.aim.dto.CreateCheckoutSessionRequest;
import com.aim.dto.CheckoutSessionResponse;
import com.aim.dto.PaymentVerificationResponse;
import com.aim.service.PaymentService;
import com.aim.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final StripeService stripeService;
    private final PaymentService paymentService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(@RequestBody CreateCheckoutSessionRequest request) {
        try {
            CheckoutSessionResponse response = stripeService.createCheckoutSession(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new CheckoutSessionResponse(null, null, "error"));
        }
    }

    @GetMapping("/verify-payment/{sessionId}")
    public ResponseEntity<PaymentVerificationResponse> verifyPayment(@PathVariable String sessionId) {
        try {
            PaymentVerificationResponse response = paymentService.processPaymentSuccess(sessionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new PaymentVerificationResponse("error", null, sessionId, 0, e.getMessage()));
        }
    }
} 
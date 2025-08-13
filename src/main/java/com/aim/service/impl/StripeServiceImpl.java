package com.aim.service.impl;

import com.aim.dto.CreateCheckoutSessionRequest;
import com.aim.dto.CheckoutSessionResponse;
import com.aim.dto.PaymentVerificationResponse;
import com.aim.service.StripeService;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Override
    public CheckoutSessionResponse createCheckoutSession(CreateCheckoutSessionRequest request) {
        try {
            Stripe.apiKey = stripeSecretKey;

            SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency(request.getCurrency())
                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(request.getDescription())
                            .build())
                    .setUnitAmount(request.getAmount().longValue())
                    .build();

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPriceData(priceData)
                    .setQuantity(1L)
                    .build();

            SessionCreateParams params = SessionCreateParams.builder()
                    .addLineItem(lineItem)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:5173/payment-success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:5173/payment-cancel")
                    .build();

            Session session = Session.create(params);

            return new CheckoutSessionResponse(
                    session.getId(),
                    session.getUrl(),
                    session.getPaymentStatus()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create checkout session: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentVerificationResponse verifyPayment(String sessionId) {
        try {
            Stripe.apiKey = stripeSecretKey;
            Session session = Session.retrieve(sessionId);

            return new PaymentVerificationResponse(
                    "payment_success",
                    "order_" + System.currentTimeMillis(),
                    sessionId,
                    session.getAmountTotal().intValue(),
                    session.getPaymentStatus()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify payment: " + e.getMessage(), e);
        }
    }
} 
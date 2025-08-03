package com.aim.service.impl;

import com.aim.config.PaymentWebSocketHandler;
import com.aim.dto.CreateCheckoutSessionRequest;
import com.aim.dto.CheckoutSessionResponse;
import com.aim.dto.PaymentVerificationResponse;
import com.aim.model.Order;
import com.aim.repository.OrderRepository;
import com.aim.service.PaymentService;
import com.aim.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final StripeService stripeService;
    private final OrderRepository orderRepository;
    private final PaymentWebSocketHandler webSocketHandler;

    @Override
    public CheckoutSessionResponse createPaymentSession(Order order, String userEmail) {
        // Convert BigDecimal to cents (Stripe expects amounts in cents)
        int amountInCents = order.getCost().multiply(BigDecimal.valueOf(100)).intValue();
        
        CreateCheckoutSessionRequest request = new CreateCheckoutSessionRequest(
                order.getOrderNumber(),
                amountInCents,
                "usd",
                "Flight Ticket: " + order.getTicketInfo()
        );

        CheckoutSessionResponse response = stripeService.createCheckoutSession(request);
        
        // Update order with Stripe session ID
        order.setStripeSessionId(response.getId());
        order.setPaymentStatus("pending");
        orderRepository.save(order);

        return response;
    }

    @Override
    public PaymentVerificationResponse processPaymentSuccess(String sessionId) {
        PaymentVerificationResponse verificationResponse = stripeService.verifyPayment(sessionId);
        
        // Find and update the order
        orderRepository.findByStripeSessionId(sessionId).ifPresent(order -> {
            updateOrderPaymentStatus(order, "paid", sessionId);
            
            // Send WebSocket notification
            String message = String.format(
                "{\"type\":\"payment_success\",\"orderId\":\"%s\",\"amount\":%d}",
                order.getOrderNumber(),
                verificationResponse.getAmount()
            );
            webSocketHandler.sendPaymentUpdate(sessionId, message);
        });

        return verificationResponse;
    }

    @Override
    public void updateOrderPaymentStatus(Order order, String paymentStatus, String sessionId) {
        order.setPaymentStatus(paymentStatus);
        order.setStripeSessionId(sessionId);
        orderRepository.save(order);
    }
} 
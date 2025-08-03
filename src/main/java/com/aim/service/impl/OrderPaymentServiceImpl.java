package com.aim.service.impl;

import com.aim.dto.UpdateOrderPaymentRequest;
import com.aim.dto.UpdateOrderPaymentResponse;
import com.aim.model.Order;
import com.aim.repository.OrderRepository;
import com.aim.service.OrderPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentServiceImpl implements OrderPaymentService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public UpdateOrderPaymentResponse updateOrderPayment(UpdateOrderPaymentRequest request) {
        log.info("Updating order payment for session: {} with status: {}", 
                request.getSessionId(), request.getPaymentStatus());
        log.info("Session ID length: {}", request.getSessionId().length());
        log.info("Full session ID: '{}'", request.getSessionId());

        try {
            // Find order by session ID
            Optional<Order> orderOpt = orderRepository.findByStripeSessionId(request.getSessionId());
            
            if (orderOpt.isEmpty()) {
                log.warn("Order not found for session ID: {}", request.getSessionId());
                log.warn("Session ID length: {}", request.getSessionId().length());
                log.warn("Full session ID: '{}'", request.getSessionId());
                
                // Debug: Let's check what session IDs exist in the database
                List<Order> allOrders = orderRepository.findAll();
                log.info("Total orders in database: {}", allOrders.size());
                for (Order order : allOrders) {
                    log.info("Order {}: session ID = '{}' (length: {})", 
                            order.getOrderNumber(), order.getStripeSessionId(), 
                            order.getStripeSessionId() != null ? order.getStripeSessionId().length() : 0);
                }
                
                return UpdateOrderPaymentResponse.builder()
                        .success(false)
                        .message("Order not found for session ID: " + request.getSessionId())
                        .build();
            }

            Order order = orderOpt.get();
            
            // Update payment status
            order.setPaymentStatus(request.getPaymentStatus());
            
            // Save the updated order
            Order savedOrder = orderRepository.save(order);
            
            log.info("Updated order payment status: {} for order: {}", 
                    request.getPaymentStatus(), savedOrder.getOrderNumber());

            return buildResponse(savedOrder, request.getSessionId(), "Payment status updated successfully", true);

        } catch (Exception e) {
            log.error("Error updating order payment: {}", e.getMessage(), e);
            return UpdateOrderPaymentResponse.builder()
                    .success(false)
                    .message("Error updating payment: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public UpdateOrderPaymentResponse updateOrderPaymentBySessionId(String sessionId, String paymentStatus) {
        log.info("Getting/updating order payment for session: {} with status: {}", sessionId, paymentStatus);

        try {
            // Find order by session ID
            Optional<Order> orderOpt = orderRepository.findByStripeSessionId(sessionId);
            
            if (orderOpt.isEmpty()) {
                log.warn("Order not found for session ID: {}", sessionId);
                return UpdateOrderPaymentResponse.builder()
                        .success(false)
                        .message("Order not found for session ID: " + sessionId)
                        .build();
            }

            Order order = orderOpt.get();
            
            // Update payment status if provided
            if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
                order.setPaymentStatus(paymentStatus);
                orderRepository.save(order);
                log.info("Updated order payment status: {} for order: {}", 
                        paymentStatus, order.getOrderNumber());
            } else {
                log.info("Retrieving order details for session: {}", sessionId);
            }

            return buildResponse(order, sessionId, 
                    paymentStatus != null ? "Payment status updated successfully" : "Order details retrieved", 
                    true);

        } catch (Exception e) {
            log.error("Error processing order payment: {}", e.getMessage(), e);
            return UpdateOrderPaymentResponse.builder()
                    .success(false)
                    .message("Error processing payment: " + e.getMessage())
                    .build();
        }
    }

    private UpdateOrderPaymentResponse buildResponse(Order order, String sessionId, String message, boolean success) {
        return UpdateOrderPaymentResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .itineraryNumber(order.getItineraryNumber())
                .passengerName(order.getFlightTicket().getPassengerName())
                .email(order.getEmail())
                .origin(order.getFlightTicket().getOrigin())
                .destination(order.getFlightTicket().getDestination())
                .airline(order.getFlightTicket().getAirline())
                .cost(order.getCost().toString())
                .paymentStatus(order.getPaymentStatus())
                .sessionId(sessionId)
                .message(message)
                .success(success)
                .build();
    }
} 
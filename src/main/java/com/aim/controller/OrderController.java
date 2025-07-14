package com.aim.controller;

import com.aim.dto.OrderRequest;
import com.aim.dto.OrderResponse;
import com.aim.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order for selected tickets
     * POST /api/v1/orders
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        log.info("Received order creation request for {} tickets", orderRequest.getTicketIds().size());
        
        OrderResponse response = orderService.createOrder(orderRequest);
        
        log.info("Order created successfully with ID: {}", response.getOrderId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
} 
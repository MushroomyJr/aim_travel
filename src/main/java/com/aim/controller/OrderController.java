package com.aim.controller;

import com.aim.dto.OrderRequest;
import com.aim.dto.OrderResponse;
import com.aim.model.Order;
import com.aim.repository.OrderRepository;
import com.aim.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    /**
     * Create a new order for selected flight tickets
     * POST /api/v1/orders
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        int ticketCount = orderRequest.getFlightTicketIds() != null ? orderRequest.getFlightTicketIds().size() : 0;
        log.info("Received order creation request for {} flight tickets for user: {}", ticketCount, orderRequest.getUserEmail());
        
        OrderResponse response = orderService.createOrder(orderRequest);
        
        log.info("Order created successfully with ID: {} and itinerary: {}", response.getOrderId(), response.getItineraryNumber());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a specific order by ID
     * GET /api/v1/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        log.info("Fetching order with ID: {}", id);
        
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            log.warn("Order not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(order.get());
    }

    /**
     * Get all orders for a user
     * GET /api/v1/orders/user/{userEmail}
     */
    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable String userEmail) {
        log.info("Fetching orders for user: {}", userEmail);
        
        List<Order> orders = orderRepository.findByUserEmail(userEmail);
        
        log.info("Found {} orders for user: {}", orders.size(), userEmail);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get order by itinerary number
     * GET /api/v1/orders/itinerary/{itineraryNumber}
     */
    @GetMapping("/itinerary/{itineraryNumber}")
    public ResponseEntity<Order> getOrderByItineraryNumber(@PathVariable String itineraryNumber) {
        log.info("Fetching order with itinerary number: {}", itineraryNumber);
        
        Optional<Order> order = orderRepository.findByItineraryNumber(itineraryNumber);
        if (order.isEmpty()) {
            log.warn("Order not found with itinerary number: {}", itineraryNumber);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(order.get());
    }
} 
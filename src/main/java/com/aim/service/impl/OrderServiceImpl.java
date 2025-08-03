package com.aim.service.impl;

import com.aim.dto.OrderRequest;
import com.aim.dto.OrderResponse;
import com.aim.model.FlightTicket;
import com.aim.model.Order;
import com.aim.model.User;
import com.aim.repository.FlightTicketRepository;
import com.aim.repository.OrderRepository;
import com.aim.repository.UserRepository;
import com.aim.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final FlightTicketRepository flightTicketRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        log.info("Creating order for user: {}", orderRequest.getUserEmail());
        
        // Validate user exists
        User user = userRepository.findByEmail(orderRequest.getUserEmail());
        if (user == null) {
            throw new IllegalArgumentException("User not found with email: " + orderRequest.getUserEmail());
        }
        
        // Validate flight tickets exist and belong to the user
        List<FlightTicket> flightTickets = validateAndGetFlightTickets(orderRequest.getFlightTicketIds(), user.getEmail());
        
        // Calculate total cost
        BigDecimal totalCost = flightTickets.stream()
                .map(FlightTicket::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Generate itinerary number if not provided
        String itineraryNumber = orderRequest.getItineraryNumber();
        if (itineraryNumber == null || itineraryNumber.trim().isEmpty()) {
            itineraryNumber = "ITIN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        
        // Generate order number
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Create orders for each flight ticket
        List<Order> orders = new ArrayList<>();
        for (FlightTicket flightTicket : flightTickets) {
            Order order = Order.builder()
                    .user(user)
                    .flightTicket(flightTicket)
                    .email(user.getEmail())
                    .ticketInfo("Flight from " + flightTicket.getOrigin() + " to " + flightTicket.getDestination())
                    .orderNumber(orderNumber)
                    .itineraryNumber(itineraryNumber)
                    .cost(flightTicket.getCost())
                    .createdAt(LocalDateTime.now())
                    .build();
            
            orders.add(order);
        }
        
        // Save all orders
        List<Order> savedOrders = orderRepository.saveAll(orders);
        
        log.info("Created {} orders with itinerary number: {}", savedOrders.size(), itineraryNumber);
        
        // Build response
        OrderResponse response = new OrderResponse();
        response.setOrderId(savedOrders.get(0).getId()); // Use first order ID as representative
        response.setUserEmail(user.getEmail());
        response.setItineraryNumber(itineraryNumber);
        response.setFlightTickets(flightTickets);
        response.setTotalCost(totalCost);
        response.setCreatedAt(LocalDateTime.now());
        response.setStatus("CONFIRMED");
        
        return response;
    }

    private List<FlightTicket> validateAndGetFlightTickets(List<Long> flightTicketIds, String userEmail) {
        if (flightTicketIds == null || flightTicketIds.isEmpty()) {
            throw new IllegalArgumentException("Flight ticket IDs cannot be null or empty");
        }
        
        List<FlightTicket> flightTickets = new ArrayList<>();
        for (Long ticketId : flightTicketIds) {
            Optional<FlightTicket> ticketOpt = flightTicketRepository.findById(ticketId);
            if (ticketOpt.isEmpty()) {
                throw new IllegalArgumentException("Flight ticket not found with ID: " + ticketId);
            }
            
            FlightTicket ticket = ticketOpt.get();
            if (!ticket.getUser().getEmail().equals(userEmail)) {
                throw new IllegalArgumentException("Flight ticket " + ticketId + " does not belong to user: " + userEmail);
            }
            
            flightTickets.add(ticket);
        }
        
        return flightTickets;
    }
} 
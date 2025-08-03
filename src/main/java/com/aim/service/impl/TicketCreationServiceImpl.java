package com.aim.service.impl;

import com.aim.dto.CreateTicketRequest;
import com.aim.dto.CreateTicketResponse;
import com.aim.dto.CreateCheckoutSessionRequest;
import com.aim.dto.CheckoutSessionResponse;
import com.aim.model.FlightTicket;
import com.aim.model.Order;
import com.aim.model.User;
import com.aim.repository.FlightTicketRepository;
import com.aim.repository.OrderRepository;
import com.aim.repository.UserRepository;
import com.aim.service.TicketCreationService;
import com.aim.service.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketCreationServiceImpl implements TicketCreationService {

    private final FlightTicketRepository flightTicketRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StripeService stripeService;

    @Override
    @Transactional
    public CreateTicketResponse createTicketWithPayment(CreateTicketRequest request) {
        log.info("Creating ticket with payment for passenger: {}", request.getPassenger());

        try {
            // Create the flight ticket
            FlightTicket ticket = createFlightTicket(request);
            
            // Create order for the ticket
            Order order = createOrder(ticket, request);
            
            // Create Stripe payment session
            CheckoutSessionResponse paymentSession = createPaymentSession(order, request);
            
            // Update order with payment session ID
            order.setStripeSessionId(paymentSession.getId());
            orderRepository.save(order);

            return CreateTicketResponse.builder()
                    .ticketId(ticket.getId())
                    .orderNumber(order.getOrderNumber())
                    .passengerName(request.getPassenger())
                    .email(request.getEmail())
                    .origin(request.getOrigin())
                    .destination(request.getDestination())
                    .airline(request.getAirline())
                    .cost(request.getCost().toString())
                    .paymentSessionUrl(paymentSession.getUrl())
                    .paymentSessionId(paymentSession.getId())
                    .status("pending_payment")
                    .build();
        } catch (Exception e) {
            log.error("Error creating ticket with payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create ticket with payment: " + e.getMessage(), e);
        }
    }

    private FlightTicket createFlightTicket(CreateTicketRequest request) {
        log.info("Creating flight ticket for passenger: {} from {} to {}", 
                request.getPassenger(), request.getOrigin(), request.getDestination());
        
        FlightTicket ticket = new FlightTicket();
        
        // Set passenger information
        ticket.setPassengerName(request.getPassenger());
        ticket.setPassengerDob(request.getDob());
        ticket.setPassengerEmail(request.getEmail());
        
        // Set flight information
        ticket.setOrigin(request.getOrigin());
        ticket.setDestination(request.getDestination());
        ticket.setRoundTrip(request.getRoundTrip());
        ticket.setDepartureTime(request.getDepartureTime());
        ticket.setArrivalTime(request.getArrivalTime());
        ticket.setReturnDepartureTime(request.getReturnDepartureTime());
        ticket.setReturnArrivalTime(request.getReturnArrivalTime());
        ticket.setAirline(request.getAirline());
        ticket.setCost(request.getCost());
        ticket.setStops(request.getStops());
        ticket.setBaggage(request.getBaggage());
        ticket.setTravelClass(request.getTravelClass());
        
        // Set user if email is provided and user exists
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            User user = userRepository.findByEmail(request.getEmail());
            if (user != null) {
                ticket.setUser(user);
                log.info("Linked ticket to existing user: {}", user.getEmail());
            } else {
                log.info("Email provided but user not found: {}", request.getEmail());
            }
        } else {
            log.info("No email provided - creating guest ticket");
        }
        
        FlightTicket savedTicket = flightTicketRepository.save(ticket);
        log.info("Created flight ticket with ID: {}", savedTicket.getId());
        
        return savedTicket;
    }

    private Order createOrder(FlightTicket ticket, CreateTicketRequest request) {
        Order order = Order.builder()
                .flightTicket(ticket)
                .email(request.getEmail() != null ? request.getEmail() : "guest@example.com")
                .ticketInfo(String.format("%s to %s - %s", request.getOrigin(), request.getDestination(), request.getAirline()))
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .itineraryNumber("ITN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .cost(request.getCost())
                .paymentStatus("pending")
                .build();
        
        // Set user if available
        if (ticket.getUser() != null) {
            order.setUser(ticket.getUser());
        }
        
        Order savedOrder = orderRepository.save(order);
        log.info("Created order with ID: {} and order number: {}", savedOrder.getId(), savedOrder.getOrderNumber());
        
        return savedOrder;
    }

    private CheckoutSessionResponse createPaymentSession(Order order, CreateTicketRequest request) {
        // Convert BigDecimal to cents (Stripe expects amounts in cents)
        int amountInCents = request.getCost().multiply(BigDecimal.valueOf(100)).intValue();
        
        CreateCheckoutSessionRequest stripeRequest = new CreateCheckoutSessionRequest(
                order.getOrderNumber(),
                amountInCents,
                "usd",
                String.format("Flight Ticket: %s to %s - %s", request.getOrigin(), request.getDestination(), request.getAirline())
        );

        CheckoutSessionResponse response = stripeService.createCheckoutSession(stripeRequest);
        log.info("Created Stripe payment session: {}", response.getId());
        
        return response;
    }
} 
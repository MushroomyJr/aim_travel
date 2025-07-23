package com.aim.service.impl;

import com.aim.dto.OrderRequest;
import com.aim.dto.OrderResponse;
import com.aim.model.FlightTicket;
import com.aim.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final Random random = new Random();

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest) {
        int ticketCount = orderRequest.getTicketIds() != null ? orderRequest.getTicketIds().size() : 0;
        log.info("Creating order for {} tickets", ticketCount);
        
        // For MVP, we'll generate mock tickets based on the ticket IDs
        List<FlightTicket> tickets = generateMockTicketsFromIds(orderRequest.getTicketIds());
        
        // Calculate total price
        BigDecimal totalPrice = tickets.stream()
                .map(FlightTicket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        String orderId = "ORDER-" + System.currentTimeMillis();
        
        OrderResponse response = new OrderResponse();
        response.setOrderId(orderId);
        response.setTickets(tickets);
        response.setTotalPrice(totalPrice);
        response.setCreatedAt(LocalDateTime.now());
        response.setUserId(orderRequest.getUserId().toString());
        
        log.info("Order created with ID: {}", orderId);
        
        return response;
    }

    private List<FlightTicket> generateMockTicketsFromIds(List<String> ticketIds) {
        List<FlightTicket> tickets = new ArrayList<>();
        
        for (String ticketId : ticketIds) {
            FlightTicket ticket = createMockTicketFromId(ticketId);
            tickets.add(ticket);
        }
        
        return tickets;
    }

    private FlightTicket createMockTicketFromId(String ticketId) {
        String[] origins = {"JFK", "LAX", "ORD", "DFW", "ATL"};
        String[] destinations = {"LAX", "JFK", "DFW", "ORD", "ATL"};
        String[] airlines = {"Delta", "American Airlines", "United", "Southwest", "JetBlue"};
        
        FlightTicket ticket = new FlightTicket();
        ticket.setId(ticketId);
        ticket.setOrigin(origins[random.nextInt(origins.length)]);
        ticket.setDestination(destinations[random.nextInt(destinations.length)]);
        ticket.setDepartureTime(LocalDateTime.now().plusDays(random.nextInt(30)));
        ticket.setArrivalTime(LocalDateTime.now().plusDays(random.nextInt(30)).plusHours(2 + random.nextInt(4)));
        ticket.setAirline(airlines[random.nextInt(airlines.length)]);
        ticket.setPrice(new BigDecimal(100 + random.nextInt(700)));
        ticket.setStops(random.nextInt(2));
        ticket.setRoundTrip(random.nextBoolean());
        
        return ticket;
    }
} 
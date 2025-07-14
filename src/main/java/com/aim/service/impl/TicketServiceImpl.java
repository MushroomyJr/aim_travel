package com.aim.service.impl;

import com.aim.config.ApiConfig;
import com.aim.dto.TicketSearchRequest;
import com.aim.dto.TicketSearchResponse;
import com.aim.model.FlightTicket;
import com.aim.service.AmadeusApiService;
import com.aim.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final ApiConfig apiConfig;
    private final AmadeusApiService amadeusApiService;
    private final Random random = new Random();

    @Override
    public TicketSearchResponse searchTickets(TicketSearchRequest searchRequest) {
        log.info("Searching tickets for: {} to {} on {}", 
                searchRequest.getOrigin(), 
                searchRequest.getDestination(), 
                searchRequest.getDepartureDate());
        
        List<FlightTicket> tickets = new ArrayList<>();
        
        // Try to get real tickets from Amadeus API if available
        if (amadeusApiService.isApiAvailable() && !apiConfig.isUseMockData()) {
            log.info("Attempting to fetch real tickets from Amadeus API");
            tickets = amadeusApiService.searchRealTickets(searchRequest);
        }
        
        // If no real tickets found or API not available, fall back to mock data
        if (tickets.isEmpty()) {
            log.info("Using mock data for ticket search");
            tickets = generateMockTickets(searchRequest);
        }
        
        log.info("Returning {} tickets", tickets.size());
        return new TicketSearchResponse(tickets);
    }

    private List<FlightTicket> generateMockTickets(TicketSearchRequest request) {
        List<FlightTicket> tickets = new ArrayList<>();
        
        // Generate 5-10 mock tickets
        int numTickets = random.nextInt(6) + 5;
        
        for (int i = 0; i < numTickets; i++) {
            FlightTicket ticket = createMockTicket(request, i);
            tickets.add(ticket);
        }
        
        return tickets;
    }

    private FlightTicket createMockTicket(TicketSearchRequest request, int index) {
        String[] airlines = {"Delta", "American Airlines", "United", "Southwest", "JetBlue"};
        String airline = airlines[random.nextInt(airlines.length)];
        
        // Generate departure time between 6 AM and 10 PM
        LocalTime departureTime = LocalTime.of(6 + random.nextInt(16), random.nextInt(4) * 15);
        LocalDateTime departureDateTime = LocalDateTime.of(request.getDepartureDate(), departureTime);
        
        // Flight duration between 1-6 hours
        int flightHours = 1 + random.nextInt(6);
        int flightMinutes = random.nextInt(60);
        LocalDateTime arrivalDateTime = departureDateTime.plusHours(flightHours).plusMinutes(flightMinutes);
        
        // Price between $100-$800
        BigDecimal price = new BigDecimal(100 + random.nextInt(700));
        
        FlightTicket ticket = new FlightTicket();
        ticket.setId("TICKET-" + System.currentTimeMillis() + "-" + index);
        ticket.setOrigin(request.getOrigin());
        ticket.setDestination(request.getDestination());
        ticket.setDepartureTime(departureDateTime);
        ticket.setArrivalTime(arrivalDateTime);
        ticket.setAirline(airline);
        ticket.setPrice(price);
        ticket.setStops(random.nextInt(2)); // 0 or 1 stops
        ticket.setRoundTrip(request.isRoundTrip());
        
        // If round trip, add return flight
        if (request.isRoundTrip() && request.getReturnDate() != null) {
            LocalTime returnDepartureTime = LocalTime.of(6 + random.nextInt(16), random.nextInt(4) * 15);
            LocalDateTime returnDepartureDateTime = LocalDateTime.of(request.getReturnDate(), returnDepartureTime);
            LocalDateTime returnArrivalDateTime = returnDepartureDateTime.plusHours(flightHours).plusMinutes(flightMinutes);
            
            ticket.setReturnDepartureTime(returnDepartureDateTime);
            ticket.setReturnArrivalTime(returnArrivalDateTime);
        }
        
        return ticket;
    }
} 
package com.aim.service.impl;

import com.aim.config.ApiConfig;
import com.aim.dto.PaginatedResponse;
import com.aim.dto.TicketSearchRequest;
import com.aim.model.FlightTicket;
import com.aim.model.User;
import com.aim.repository.FlightTicketRepository;
import com.aim.repository.UserRepository;
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
    private final FlightTicketRepository flightTicketRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    @Override
    public PaginatedResponse<FlightTicket> searchTickets(TicketSearchRequest searchRequest) {
        log.info("Searching tickets from {} to {} on {}", 
                searchRequest.getOrigin(), 
                searchRequest.getDestination(), 
                searchRequest.getDepartureDate());

        List<FlightTicket> allTickets = new ArrayList<>();

        // Try to get real tickets from Amadeus API first
        if (amadeusApiService.isApiAvailable()) {
            log.info("Amadeus API is available, searching for real tickets");
            allTickets = amadeusApiService.searchRealTickets(searchRequest);
        }

        // If no real tickets found, generate mock tickets
        if (allTickets.isEmpty()) {
            log.info("No real tickets found, generating mock tickets");
            allTickets = generateMockTickets(searchRequest);
        }
        
        // Apply pagination
        List<FlightTicket> paginatedTickets = applyPagination(allTickets, searchRequest.getPage(), searchRequest.getSize());
        
        // Create pagination metadata
        PaginatedResponse.PaginationMetadata metadata = createPaginationMetadata(
                allTickets.size(), 
                searchRequest.getPage(), 
                searchRequest.getSize()
        );
        
        log.info("Returning {} tickets (page {} of {})", 
                paginatedTickets.size(), 
                metadata.getPage() + 1, 
                metadata.getTotalPages());
        
        return new PaginatedResponse<>(paginatedTickets, metadata);
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
        
        // Cost between $100-$800
        BigDecimal cost = new BigDecimal(100 + random.nextInt(700));
        
        FlightTicket ticket = new FlightTicket();
        ticket.setOrigin(request.getOrigin());
        ticket.setDestination(request.getDestination());
        ticket.setDepartureTime(departureDateTime);
        ticket.setArrivalTime(arrivalDateTime);
        ticket.setAirline(airline);
        ticket.setCost(cost);
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
    
    /**
     * Apply pagination to a list of tickets
     */
    private List<FlightTicket> applyPagination(List<FlightTicket> tickets, int page, int size) {
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, tickets.size());
        
        if (startIndex >= tickets.size()) {
            return new ArrayList<>();
        }
        
        return tickets.subList(startIndex, endIndex);
    }
    
    /**
     * Create pagination metadata
     */
    private PaginatedResponse.PaginationMetadata createPaginationMetadata(int totalElements, int page, int size) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;
        
        return new PaginatedResponse.PaginationMetadata(page, size, totalElements, totalPages, hasNext, hasPrevious);
    }
} 
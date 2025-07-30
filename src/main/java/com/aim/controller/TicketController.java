package com.aim.controller;

import com.aim.dto.TicketSearchRequest;
import com.aim.dto.PaginatedResponse;
import com.aim.model.FlightTicket;
import com.aim.model.User;
import com.aim.repository.FlightTicketRepository;
import com.aim.repository.UserRepository;
import com.aim.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;
    private final FlightTicketRepository flightTicketRepository;
    private final UserRepository userRepository;

    /**
     * Search for available tickets based on criteria with pagination
     * POST /api/v1/tickets/search
     */
    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse<FlightTicket>> searchTickets(@Valid @RequestBody TicketSearchRequest searchRequest) {
        log.info("Received ticket search request: {} to {} on {} (page: {}, size: {})", 
                searchRequest.getOrigin(), 
                searchRequest.getDestination(), 
                searchRequest.getDepartureDate(),
                searchRequest.getPage(),
                searchRequest.getSize());
        
        PaginatedResponse<FlightTicket> response = ticketService.searchTickets(searchRequest);
        
        log.info("Found {} tickets matching criteria (page {} of {})", 
                response.getData().size(),
                response.getPagination().getPage() + 1,
                response.getPagination().getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new flight ticket for a user
     * POST /api/v1/tickets
     */
    @PostMapping
    public ResponseEntity<FlightTicket> createFlightTicket(@Valid @RequestBody FlightTicket flightTicket, 
                                                         @RequestParam String userEmail) {
        log.info("Creating flight ticket for user: {}", userEmail);
        
        // Validate user exists
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            log.error("User not found with email: {}", userEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
        
        flightTicket.setUser(user);
        FlightTicket savedTicket = flightTicketRepository.save(flightTicket);
        
        log.info("Flight ticket created successfully with ID: {}", savedTicket.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTicket);
    }

    /**
     * Get a specific flight ticket by ID
     * GET /api/v1/tickets/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<FlightTicket> getFlightTicket(@PathVariable Long id) {
        log.info("Fetching flight ticket with ID: {}", id);
        
        Optional<FlightTicket> ticket = flightTicketRepository.findById(id);
        if (ticket.isEmpty()) {
            log.warn("Flight ticket not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(ticket.get());
    }

    /**
     * Get all flight tickets for a user
     * GET /api/v1/tickets/user/{userEmail}
     */
    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<FlightTicket>> getUserFlightTickets(@PathVariable String userEmail) {
        log.info("Fetching flight tickets for user: {}", userEmail);
        
        List<FlightTicket> tickets = flightTicketRepository.findByUserEmail(userEmail);
        
        log.info("Found {} flight tickets for user: {}", tickets.size(), userEmail);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Update a flight ticket
     * PUT /api/v1/tickets/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<FlightTicket> updateFlightTicket(@PathVariable Long id, 
                                                         @Valid @RequestBody FlightTicket updatedTicket) {
        log.info("Updating flight ticket with ID: {}", id);
        
        Optional<FlightTicket> existingTicket = flightTicketRepository.findById(id);
        if (existingTicket.isEmpty()) {
            log.warn("Flight ticket not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        FlightTicket ticket = existingTicket.get();
        
        // Update fields (preserve user relationship)
        ticket.setOrigin(updatedTicket.getOrigin());
        ticket.setDestination(updatedTicket.getDestination());
        ticket.setRoundTrip(updatedTicket.isRoundTrip());
        ticket.setDepartureTime(updatedTicket.getDepartureTime());
        ticket.setArrivalTime(updatedTicket.getArrivalTime());
        ticket.setReturnDepartureTime(updatedTicket.getReturnDepartureTime());
        ticket.setReturnArrivalTime(updatedTicket.getReturnArrivalTime());
        ticket.setAirline(updatedTicket.getAirline());
        ticket.setCost(updatedTicket.getCost());
        ticket.setStops(updatedTicket.getStops());
        ticket.setBaggage(updatedTicket.getBaggage());
        ticket.setTravelClass(updatedTicket.getTravelClass());
        
        FlightTicket savedTicket = flightTicketRepository.save(ticket);
        
        log.info("Flight ticket updated successfully with ID: {}", savedTicket.getId());
        return ResponseEntity.ok(savedTicket);
    }

    /**
     * Delete a flight ticket
     * DELETE /api/v1/tickets/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlightTicket(@PathVariable Long id) {
        log.info("Deleting flight ticket with ID: {}", id);
        
        if (!flightTicketRepository.existsById(id)) {
            log.warn("Flight ticket not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        flightTicketRepository.deleteById(id);
        
        log.info("Flight ticket deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }
} 
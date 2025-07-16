package com.aim.controller;

import com.aim.dto.TicketSearchRequest;
import com.aim.dto.PaginatedResponse;
import com.aim.model.FlightTicket;
import com.aim.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

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
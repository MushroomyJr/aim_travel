package com.aim.controller;

import com.aim.dto.TicketSearchRequest;
import com.aim.dto.TicketSearchResponse;
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
     * Search for available tickets based on criteria
     * POST /api/v1/tickets/search
     */
    @PostMapping("/search")
    public ResponseEntity<TicketSearchResponse> searchTickets(@Valid @RequestBody TicketSearchRequest searchRequest) {
        log.info("Received ticket search request: {} to {} on {}", 
                searchRequest.getOrigin(), 
                searchRequest.getDestination(), 
                searchRequest.getDepartureDate());
        
        TicketSearchResponse response = ticketService.searchTickets(searchRequest);
        
        log.info("Found {} tickets matching criteria", response.getTickets().size());
        
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
package com.aim.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketSearchRequest {
    @NotBlank(message = "Origin is required")
    private String origin;
    
    @NotBlank(message = "Destination is required")
    private String destination;
    
    @NotNull(message = "Departure date is required")
    @FutureOrPresent(message = "Departure date must be today or in the future")
    private LocalDate departureDate;
    
    private LocalDate returnDate; // nullable for one-way
    
    private boolean roundTrip;
    
    @Min(value = 1, message = "At least 1 passenger is required")
    private int passengers = 1;
} 
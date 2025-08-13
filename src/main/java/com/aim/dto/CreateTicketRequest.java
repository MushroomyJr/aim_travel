package com.aim.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {
    
    @NotBlank(message = "Passenger name is required")
    private String passenger;
    
    @NotNull(message = "Date of birth is required")
    private LocalDate dob;
    
    @Email(message = "Email should be valid")
    private String email; // Optional for guest users
    
    @NotBlank(message = "Origin is required")
    private String origin;
    
    @NotBlank(message = "Destination is required")
    private String destination;
    
    @NotNull(message = "Round trip flag is required")
    private Boolean roundTrip;
    
    @NotNull(message = "Departure time is required")
    private LocalDateTime departureTime;
    
    @NotNull(message = "Arrival time is required")
    private LocalDateTime arrivalTime;
    
    private LocalDateTime returnDepartureTime; // Optional for round trips
    
    private LocalDateTime returnArrivalTime; // Optional for round trips
    
    @NotBlank(message = "Airline is required")
    private String airline;
    
    @NotNull(message = "Cost is required")
    private BigDecimal cost;
    
    @NotNull(message = "Number of stops is required")
    private Integer stops;
    
    private String baggage; // Optional
    
    private String travelClass; // Optional
} 
package com.aim.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightTicket {
    private String id;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String airline;
    private BigDecimal price;
    private int stops;
    private boolean roundTrip;
    private LocalDateTime returnDepartureTime; // nullable for one-way
    private LocalDateTime returnArrivalTime;   // nullable for one-way
} 
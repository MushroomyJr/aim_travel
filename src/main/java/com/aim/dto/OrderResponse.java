package com.aim.dto;

import com.aim.model.FlightTicket;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long orderId;
    private String userEmail;
    private String itineraryNumber;
    private List<FlightTicket> flightTickets;
    private BigDecimal totalCost;
    private LocalDateTime createdAt;
    private String status;
} 
package com.aim.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String id;
    private String userId; // optional for now
    private List<FlightTicket> tickets;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
} 
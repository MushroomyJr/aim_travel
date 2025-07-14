package com.aim.dto;

import com.aim.model.FlightTicket;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private List<FlightTicket> tickets;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private String userId;
} 
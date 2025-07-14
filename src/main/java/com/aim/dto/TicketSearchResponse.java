package com.aim.dto;

import com.aim.model.FlightTicket;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketSearchResponse {
    private List<FlightTicket> tickets;
} 
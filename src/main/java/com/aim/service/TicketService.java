package com.aim.service;

import com.aim.dto.TicketSearchRequest;
import com.aim.dto.PaginatedResponse;
import com.aim.model.FlightTicket;

public interface TicketService {
    
    /**
     * Search for available tickets based on the provided criteria with pagination
     * @param searchRequest The search criteria including origin, destination, dates, trip type, and pagination parameters
     * @return Paginated response with matching tickets
     */
    PaginatedResponse<FlightTicket> searchTickets(TicketSearchRequest searchRequest);
} 
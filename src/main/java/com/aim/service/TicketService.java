package com.aim.service;

import com.aim.dto.TicketSearchRequest;
import com.aim.dto.TicketSearchResponse;

public interface TicketService {
    
    /**
     * Search for available tickets based on the provided criteria
     * @param searchRequest The search criteria including origin, destination, dates, and trip type
     * @return Search response with matching tickets
     */
    TicketSearchResponse searchTickets(TicketSearchRequest searchRequest);
} 
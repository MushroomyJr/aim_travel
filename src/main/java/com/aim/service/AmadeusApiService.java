package com.aim.service;

import com.aim.dto.TicketSearchRequest;
import com.aim.model.FlightTicket;
import java.util.List;

public interface AmadeusApiService {
    
    /**
     * Search for real flight tickets using Amadeus API
     * @param searchRequest The search criteria
     * @return List of real flight tickets from the API
     */
    List<FlightTicket> searchRealTickets(TicketSearchRequest searchRequest);
    
    /**
     * Check if the API is available and configured
     * @return true if API is available, false otherwise
     */
    boolean isApiAvailable();
} 
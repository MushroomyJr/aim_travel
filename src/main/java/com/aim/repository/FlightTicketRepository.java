package com.aim.repository;

import com.aim.model.FlightTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlightTicketRepository extends JpaRepository<FlightTicket, Long> {
    
    List<FlightTicket> findByUserEmail(String email);
    
    List<FlightTicket> findByOriginAndDestination(String origin, String destination);
    
    List<FlightTicket> findByUserEmailAndRoundTrip(String email, boolean roundTrip);
} 
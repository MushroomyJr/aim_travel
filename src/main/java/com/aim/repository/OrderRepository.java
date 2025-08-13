package com.aim.repository;

import com.aim.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserEmail(String email);
    
    Optional<Order> findByItineraryNumber(String itineraryNumber);
    
    List<Order> findByUserEmailOrderByCreatedAtDesc(String email);
    
    Optional<Order> findByStripeSessionId(String stripeSessionId);
} 
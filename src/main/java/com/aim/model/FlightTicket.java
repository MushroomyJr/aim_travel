package com.aim.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "flight_ticket")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = true)
    private User user; // Optional for guest users

    @Column(name = "passenger_name", nullable = false)
    private String passengerName;

    @Column(name = "passenger_dob", nullable = false)
    private java.time.LocalDate passengerDob;

    @Column(name = "passenger_email", nullable = true)
    private String passengerEmail; // Optional for guest users

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private boolean roundTrip;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    @Column(nullable = true)
    private LocalDateTime returnDepartureTime;

    @Column(nullable = true)
    private LocalDateTime returnArrivalTime;

    @Column(nullable = false)
    private String airline;

    @Column(nullable = false)
    private BigDecimal cost;

    // JSON field alias for frontend compatibility
    @com.fasterxml.jackson.annotation.JsonProperty("price")
    public BigDecimal getPrice() {
        return cost;
    }

    @Column(nullable = false)
    private int stops;

    // Optional fields for future expansion
    @Column(nullable = true)
    private String baggage;

    @Column(nullable = true)
    private String travelClass;
    
    @Column(nullable = true)
    private String duration; // Flight duration in ISO 8601 format (e.g., "PT5H7M")
    
    // Segment information for detailed flight routing
    @Transient // Don't persist to database, only for API response
    private List<FlightSegment> outboundSegments;
    
    @Transient // Don't persist to database, only for API response
    private List<FlightSegment> returnSegments;
    
    // Inner class for flight segments
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlightSegment {
        private String departureAirport;
        private String arrivalAirport;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private String airline;
        private String flightNumber;
        private String duration;
        private String aircraft;
        private String terminal;
        private String gate;
    }
} 
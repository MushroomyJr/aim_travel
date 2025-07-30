package com.aim.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_ticket", referencedColumnName = "id", nullable = false)
    private FlightTicket flightTicket;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "ticket_info", nullable = false)
    private String ticketInfo;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "itenary_number", nullable = false, unique = true)
    private String itineraryNumber;

    @Column(nullable = false)
    private BigDecimal cost;

    // Optional fields for future expansion
    @Column(nullable = true)
    private String hotelStayOrder;

    @Column(nullable = true)
    private String rentalOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 
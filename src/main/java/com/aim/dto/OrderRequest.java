package com.aim.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String userEmail;
    private List<Long> flightTicketIds; 
    private String itineraryNumber;
} 
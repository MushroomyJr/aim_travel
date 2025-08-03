package com.aim.service;

import com.aim.dto.CreateTicketRequest;
import com.aim.dto.CreateTicketResponse;

public interface TicketCreationService {
    CreateTicketResponse createTicketWithPayment(CreateTicketRequest request);
} 
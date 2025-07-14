package com.aim.service;

import com.aim.dto.OrderRequest;
import com.aim.dto.OrderResponse;

public interface OrderService {
    
    /**
     * Create a new order for selected tickets
     * @param orderRequest The order request containing ticket IDs and user info
     * @return Order response with order details
     */
    OrderResponse createOrder(OrderRequest orderRequest);
} 
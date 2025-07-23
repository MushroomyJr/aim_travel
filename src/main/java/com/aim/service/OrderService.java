package com.aim.service;

import com.aim.dto.OrderRequest;
import com.aim.dto.OrderResponse;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
}
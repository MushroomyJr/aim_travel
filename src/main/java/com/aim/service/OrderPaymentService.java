package com.aim.service;

import com.aim.dto.UpdateOrderPaymentRequest;
import com.aim.dto.UpdateOrderPaymentResponse;

public interface OrderPaymentService {
    UpdateOrderPaymentResponse updateOrderPayment(UpdateOrderPaymentRequest request);
    UpdateOrderPaymentResponse updateOrderPaymentBySessionId(String sessionId, String paymentStatus);
} 
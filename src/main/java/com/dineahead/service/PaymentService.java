package com.dineahead.service;

import com.dineahead.dto.PaymentRequestDTO;
import com.dineahead.dto.PaymentResponseDTO;

public interface PaymentService {

    PaymentResponseDTO createPayment(
            PaymentRequestDTO request
    );

    PaymentResponseDTO getPaymentByOrderId(
            Long orderId
    );

    PaymentResponseDTO completePayment(Long paymentId);
}
package com.dineahead.dto;

import com.dineahead.enums.PaymentMethod;
import com.dineahead.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private Long id;

    private Long orderId;

    private BigDecimal amount;

    private PaymentMethod paymentMethod;

    private PaymentStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
package com.dineahead.dto;

import com.dineahead.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    @NotNull
    private Long orderId;

    @NotNull
    private PaymentMethod paymentMethod;
}
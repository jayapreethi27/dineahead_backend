package com.dineahead.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantOrderDashboardDTO {

    private OrderResponseDTO order;

    private PaymentResponseDTO payment;
}
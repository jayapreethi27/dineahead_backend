package com.dineahead.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDashboardDTO {

    private Long restaurantId;

    private List<ReservationResponseDTO> reservations;

    private List<RestaurantOrderDashboardDTO> orders;
}
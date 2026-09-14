package com.dineahead.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTableResponseDTO {

    private Long id;
    private Integer tableNumber;
    private Integer capacity;
    private Long restaurantId;
}

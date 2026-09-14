package com.dineahead.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantMenuResponseDTO {

    private Long restaurantId;
    private String restaurantName;
    private List<RestaurantMenuCategoryDTO> categories;
}

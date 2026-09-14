package com.dineahead.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantMenuItemDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean available;
}

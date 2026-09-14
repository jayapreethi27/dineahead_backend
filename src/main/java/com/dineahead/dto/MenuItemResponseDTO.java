package com.dineahead.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponseDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean available;
    private Long categoryId;
}

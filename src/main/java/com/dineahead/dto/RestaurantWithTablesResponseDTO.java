package com.dineahead.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantWithTablesResponseDTO {

    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;

    private List<TableSummaryDTO> tables;
}

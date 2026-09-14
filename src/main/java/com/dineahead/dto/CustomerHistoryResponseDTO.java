package com.dineahead.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerHistoryResponseDTO {

    private String customerName;

    private List<CustomerHistoryReservationDTO> reservations;
}
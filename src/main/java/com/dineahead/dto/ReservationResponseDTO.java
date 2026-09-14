package com.dineahead.dto;

import com.dineahead.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDTO {

    private Long id;
    private String customerName;
    private LocalDateTime reservationTime;
    private Integer partySize;
    private Long restaurantId;
    private Long restaurantTableId;
    private Integer tableNumber;
    private ReservationStatus status;
}

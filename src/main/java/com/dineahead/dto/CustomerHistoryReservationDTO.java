package com.dineahead.dto;

import com.dineahead.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerHistoryReservationDTO {

    private Long reservationId;

    private String customerName;

    private LocalDateTime reservationTime;

    private Integer partySize;

    private Long restaurantId;

    private Long restaurantTableId;

    private ReservationStatus reservationStatus;

    private OrderResponseDTO order;

    private PaymentResponseDTO payment;
}
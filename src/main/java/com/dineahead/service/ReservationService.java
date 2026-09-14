package com.dineahead.service;

import com.dineahead.dto.CustomerHistoryResponseDTO;
import com.dineahead.dto.ReservationRequestDTO;
import com.dineahead.dto.ReservationResponseDTO;

import java.util.List;

public interface ReservationService {

    ReservationResponseDTO saveReservation(
            ReservationRequestDTO request
    );

    ReservationResponseDTO getReservationById(Long id);

    List<ReservationResponseDTO> getAllReservations();

    ReservationResponseDTO updateReservation(
            Long id,
            ReservationRequestDTO request
    );

    void deleteReservation(Long id);

    List<ReservationResponseDTO> getReservationsByRestaurantId(
            Long restaurantId
    );

    List<ReservationResponseDTO> getTodaysReservationsByRestaurantId(
            Long restaurantId
    );

    List<ReservationResponseDTO> getReservationsByCustomerName(
            String customerName
    );

    void cancelReservation(Long id);

    void completeReservation(Long id);

    CustomerHistoryResponseDTO getCustomerHistory(
            String customerName
    );
}

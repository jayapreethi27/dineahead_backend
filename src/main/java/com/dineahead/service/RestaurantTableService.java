package com.dineahead.service;

import com.dineahead.dto.RestaurantTableRequestDTO;
import com.dineahead.dto.RestaurantTableResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface RestaurantTableService {

    RestaurantTableResponseDTO saveRestaurantTable(
            RestaurantTableRequestDTO request
    );

    RestaurantTableResponseDTO getRestaurantTableById(Long id);

    List<RestaurantTableResponseDTO> getTablesByRestaurantId(
            Long restaurantId
    );

    RestaurantTableResponseDTO updatedRestaurantTable(Long id, RestaurantTableRequestDTO request);

    void deleteRestaurantTable(Long id);

    List<RestaurantTableResponseDTO> getAvailableTables(
            Long restaurantId,
            LocalDateTime reservationTime,
            Integer partySize
    );
}
package com.dineahead.service;

import com.dineahead.dto.RestaurantMenuResponseDTO;
import com.dineahead.dto.RestaurantRequestDTO;
import com.dineahead.dto.RestaurantResponseDTO;
import com.dineahead.dto.RestaurantWithTablesResponseDTO;

import java.util.List;

public interface RestaurantService {

    RestaurantResponseDTO saveRestaurant(RestaurantRequestDTO request);

    RestaurantResponseDTO getRestaurantById(Long id);

    RestaurantResponseDTO updateRestaurant(Long id, RestaurantRequestDTO request);

    void deleteRestaurant(Long id);

    RestaurantWithTablesResponseDTO getRestaurantWithTables(Long id);

    List<RestaurantResponseDTO> getAllRestaurants();

    RestaurantMenuResponseDTO getRestaurantMenu(Long restaurantId);

    RestaurantResponseDTO assignOwner(
            Long restaurantId,
            Long ownerId
    );

}

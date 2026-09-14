package com.dineahead.repository;

import com.dineahead.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByRestaurantId(Long restaurantId);

    boolean existsByRestaurantIdAndTableNumber(
            Long restaurantId,
            Integer tableNumber
    );

    List<RestaurantTable> findByRestaurantIdAndCapacityGreaterThanEqual(
            Long restaurantId,
            Integer partySize
    );

    boolean existsByRestaurantIdAndTableNumberAndIdNot(
            Long restaurantId,
            Integer tableNumber,
            Long id
    );
}

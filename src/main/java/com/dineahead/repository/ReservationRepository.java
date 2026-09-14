package com.dineahead.repository;

import com.dineahead.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByRestaurantTableIdAndReservationTime(
            Long restaurantTableId,
            LocalDateTime reservationTime
    );

    boolean existsByRestaurantTableIdAndReservationTimeAndIdNot(
            Long restaurantTableId,
            LocalDateTime reservationTime,
            Long id
    );

    List<Reservation> findByRestaurantTable_Id(Long restaurantTableId);

    List<Reservation> findByRestaurantId(Long restaurantId);

    List<Reservation> findByCustomerName(String customerName);

    List<Reservation> findByRestaurantIdAndReservationTimeBetween(
            Long restaurantId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Reservation> findByUserId(Long userId);
}

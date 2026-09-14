package com.dineahead.repository;

import com.dineahead.entity.Order;
import com.dineahead.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    //List<Order> findByReservationId(Long reservationId);

    List<Order> findByStatusIn(
            List<OrderStatus> statuses
    );

    List<Order> findByReservationRestaurantIdAndReservationReservationTimeBetween(
            Long restaurantId,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<Order> findByReservationId(
            Long reservationId
    );

}
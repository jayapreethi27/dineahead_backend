package com.dineahead.service;

import com.dineahead.entity.Reservation;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class ReservationOrderWindowService {

    public boolean isOrderingOpen(
            Reservation reservation) {

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime reservationTime =
                reservation.getReservationTime();

        LocalDateTime orderingOpenTime =
                reservationTime.minusHours(1);


        // Ordering must be at least within
        // one hour before the reservation
        if (now.isBefore(orderingOpenTime)) {

            return false;
        }


        // Ordering must close when the
        // reservation time is reached
        if (!now.isBefore(reservationTime)) {

            return false;
        }


        return true;
    }
}
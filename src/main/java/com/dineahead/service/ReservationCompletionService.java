package com.dineahead.service;

import com.dineahead.entity.Reservation;

public interface ReservationCompletionService {

    void completeReservationIfEligible(
            Reservation reservation
    );
}
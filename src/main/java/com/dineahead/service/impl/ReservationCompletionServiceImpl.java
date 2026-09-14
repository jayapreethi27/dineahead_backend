package com.dineahead.service.impl;

import com.dineahead.entity.Order;
import com.dineahead.entity.Payment;
import com.dineahead.entity.Reservation;
import com.dineahead.enums.OrderStatus;
import com.dineahead.enums.PaymentStatus;
import com.dineahead.enums.ReservationStatus;
import com.dineahead.repository.OrderRepository;
import com.dineahead.repository.PaymentRepository;
import com.dineahead.repository.ReservationRepository;
import com.dineahead.service.ReservationCompletionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReservationCompletionServiceImpl
        implements ReservationCompletionService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;


    @Override
    public void completeReservationIfEligible(
            Reservation reservation) {

        // Do not modify cancelled reservations
        if (reservation.getStatus()
                == ReservationStatus.CANCELLED) {

            return;
        }


        // Reservation is already completed
        if (reservation.getStatus()
                == ReservationStatus.COMPLETED) {

            return;
        }


        /*
         * Find the order belonging to this reservation.
         *
         * If your OrderRepository does not yet have
         * findByReservationId(), we will add it below.
         */
        Optional<Order> orderOptional =
                orderRepository.findByReservationId(
                        reservation.getId()
                );


        // No order means reservation cannot be completed yet
        if (orderOptional.isEmpty()) {

            return;
        }


        Order order =
                orderOptional.get();


        // Order must be served
        if (order.getStatus()
                != OrderStatus.SERVED) {

            return;
        }


        // Find payment for this order
        Optional<Payment> paymentOptional =
                paymentRepository.findByOrderId(
                        order.getId()
                );


        // No payment means reservation cannot be completed
        if (paymentOptional.isEmpty()) {

            return;
        }


        Payment payment =
                paymentOptional.get();


        // Payment must be completed
        if (payment.getStatus()
                != PaymentStatus.COMPLETED) {

            return;
        }


        // Both conditions are satisfied
        reservation.setStatus(
                ReservationStatus.COMPLETED
        );

        reservationRepository.save(
                reservation
        );
    }
}
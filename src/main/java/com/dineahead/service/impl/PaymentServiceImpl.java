package com.dineahead.service.impl;

import com.dineahead.dto.PaymentRequestDTO;
import com.dineahead.dto.PaymentResponseDTO;
import com.dineahead.entity.Order;
import com.dineahead.entity.Payment;
import com.dineahead.entity.Reservation;
import com.dineahead.enums.OrderStatus;
import com.dineahead.enums.PaymentMethod;
import com.dineahead.enums.PaymentStatus;
import com.dineahead.enums.ReservationStatus;
import com.dineahead.repository.OrderRepository;
import com.dineahead.repository.PaymentRepository;
import com.dineahead.repository.ReservationRepository;
import com.dineahead.service.PaymentService;

import com.dineahead.service.ReservationCompletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.dineahead.entity.User;
import com.dineahead.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.dineahead.enums.NotificationType;


@Service
public class PaymentServiceImpl
        implements PaymentService {


    @Autowired
    private PaymentRepository paymentRepository;


    @Autowired
    private OrderRepository orderRepository;


    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationCompletionService
            reservationCompletionService;

    @Autowired
    private UserRepository userRepository;


    // =====================================================
    // CREATE PAYMENT
    // =====================================================

    @Override
    public PaymentResponseDTO createPayment(
            PaymentRequestDTO request) {


        // 1. Find order
        Order order =
                orderRepository.findById(
                                request.getOrderId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Order with id "
                                                + request.getOrderId()
                                                + " not found"
                                )
                        );

        validatePaymentCreationAccess(order);


        // 2. Prevent duplicate payment
        if (paymentRepository
                .existsByOrderId(order.getId())) {

            throw new IllegalStateException(
                    "Payment already exists for order "
                            + order.getId()
            );
        }


        // 3. Get reservation
        Reservation reservation =
                order.getReservation();


        // 4. Reservation must not be cancelled
        if (reservation.getStatus()
                == ReservationStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Payment cannot be created for "
                            + "a cancelled reservation."
            );
        }


        // 5. Reservation must be confirmed
        if (reservation.getStatus()
                != ReservationStatus.CONFIRMED) {

            throw new IllegalStateException(
                    "Payment can only be created for "
                            + "a confirmed reservation."
            );
        }


        // 6. Order must be in a valid status
        if (order.getStatus()
                != OrderStatus.PLACED) {

            throw new IllegalStateException(
                    "Payment cannot be created for order "
                            + "with status "
                            + order.getStatus()
            );
        }


        // 7. Order total must be valid
        if (order.getTotalAmount() == null
                || order.getTotalAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalStateException(
                    "Payment cannot be created because "
                            + "the order total amount is invalid."
            );
        }


        // 8. Create payment
        Payment payment =
                new Payment();


        payment.setOrder(order);


        // Always take the amount from backend/database
        payment.setAmount(
                order.getTotalAmount()
        );


        payment.setPaymentMethod(
                request.getPaymentMethod()
        );


        payment.setStatus(
                PaymentStatus.PENDING
        );


        payment.setCreatedAt(
                LocalDateTime.now()
        );


        // 9. Save payment
        Payment savedPayment =
                paymentRepository.save(
                        payment
                );


        // 10. Return response
        return mapToResponseDTO(
                savedPayment
        );
    }


    // =====================================================
    // GET PAYMENT BY ORDER ID
    // =====================================================

    @Override
    public PaymentResponseDTO getPaymentByOrderId(
            Long orderId) {


        Payment payment =
                paymentRepository
                        .findByOrderId(orderId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Payment for order "
                                                + orderId
                                                + " not found"
                                )
                        );


        validatePaymentAccess(
                payment
        );

        return mapToResponseDTO(
                payment
        );
    }


    // =====================================================
    // COMPLETE PAYMENT
    // =====================================================

    @Override
    public PaymentResponseDTO completePayment(
            Long paymentId) {

        // 1. Find payment
        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Payment with id "
                                                + paymentId
                                                + " not found"
                                )
                        );

        validatePaymentAccess(
                payment
        );


        // 2. Payment must not already be completed
        if (payment.getStatus()
                == PaymentStatus.COMPLETED) {

            throw new IllegalStateException(
                    "Payment is already completed."
            );
        }


        // 3. Get related order
        Order order =
                payment.getOrder();


        // 4. Cancelled orders cannot be paid
        if (order.getStatus()
                == OrderStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Payment cannot be completed for a cancelled order."
            );
        }


        // 5. Get reservation
        Reservation reservation =
                order.getReservation();


        // 6. Cancelled reservations cannot be paid
        if (reservation.getStatus()
                == ReservationStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Payment cannot be completed for a cancelled reservation."
            );
        }


        // 7. AFTER_MEAL payment can only be completed
        // after the food has been served
        if (payment.getPaymentMethod()
                == PaymentMethod.AFTER_MEAL
                &&
                order.getStatus()
                        != OrderStatus.SERVED) {

            throw new IllegalStateException(
                    "AFTER_MEAL payment can only be completed "
                            + "after the order has been served."
            );
        }


        // 8. Complete payment
        payment.setStatus(
                PaymentStatus.COMPLETED
        );

        payment.setCompletedAt(
                LocalDateTime.now()
        );


        // 9. Save payment
        Payment completedPayment =
                paymentRepository.save(
                        payment
                );


        //Check whether reservation can now be completed.

        reservationCompletionService
                .completeReservationIfEligible(
                        reservation
                );


        // 10. Return response
        return mapToResponseDTO(
                completedPayment
        );
    }



    private User getCurrentAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        String email =
                authentication.getName();


        return userRepository
                .findByEmail(
                        email
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found."
                        )
                );
    }

    private boolean isAdmin() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(
                        authority ->
                                authority.getAuthority()
                                        .equals(
                                                "ROLE_ADMIN"
                                        )
                );
    }

    private boolean isRestaurantOwner() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(
                        authority ->
                                authority.getAuthority()
                                        .equals(
                                                "ROLE_RESTAURANT_OWNER"
                                        )
                );
    }

    private void validatePaymentCreationAccess(
            Order order
    ) {

        // ADMIN can create payment for any order

        if (isAdmin()) {

            return;
        }


        // RESTAURANT OWNER cannot create payment

        if (isRestaurantOwner()) {

            throw new AccessDeniedException(
                    "Restaurant owners cannot create payments."
            );
        }


        // CUSTOMER can create payment
        // only for their own order

        User currentUser =
                getCurrentAuthenticatedUser();


        if (order.getReservation()
                .getUser() == null
                ||
                !order.getReservation()
                        .getUser()
                        .getId()
                        .equals(
                                currentUser.getId()
                        )) {

            throw new AccessDeniedException(
                    "You do not have permission to create "
                            + "a payment for this order."
            );
        }
    }

    private void validatePaymentAccess(
            Payment payment
    ) {

        // ADMIN can access every payment

        if (isAdmin()) {

            return;
        }


        User currentUser =
                getCurrentAuthenticatedUser();


        Order order =
                payment.getOrder();


        // RESTAURANT OWNER can access payments
        // belonging to their restaurant

        if (isRestaurantOwner()) {

            if (order.getReservation()
                    .getRestaurant()
                    .getOwner() != null
                    &&
                    order.getReservation()
                            .getRestaurant()
                            .getOwner()
                            .getId()
                            .equals(
                                    currentUser.getId()
                            )) {

                return;
            }


            throw new AccessDeniedException(
                    "You do not have permission to access this payment."
            );
        }


        // CUSTOMER can access only their own payment

        if (order.getReservation()
                .getUser() != null
                &&
                order.getReservation()
                        .getUser()
                        .getId()
                        .equals(
                                currentUser.getId()
                        )) {

            return;
        }


        throw new AccessDeniedException(
                "You do not have permission to access this payment."
        );
    }


    // =====================================================
    // MAP TO RESPONSE DTO
    // =====================================================

    private PaymentResponseDTO mapToResponseDTO(
            Payment payment) {


        PaymentResponseDTO response =
                new PaymentResponseDTO();


        response.setId(
                payment.getId()
        );


        response.setOrderId(
                payment.getOrder()
                        .getId()
        );


        response.setAmount(
                payment.getAmount()
        );


        response.setPaymentMethod(
                payment.getPaymentMethod()
        );


        response.setStatus(
                payment.getStatus()
        );


        response.setCreatedAt(
                payment.getCreatedAt()
        );


        response.setCompletedAt(
                payment.getCompletedAt()
        );


        return response;
    }
}
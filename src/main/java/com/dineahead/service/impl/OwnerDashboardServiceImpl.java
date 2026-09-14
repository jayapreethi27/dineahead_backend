package com.dineahead.service.impl;

import com.dineahead.dto.dashboard.OwnerDashboardResponseDTO;

import com.dineahead.entity.Order;
import com.dineahead.entity.Payment;
import com.dineahead.entity.Reservation;
import com.dineahead.entity.Restaurant;
import com.dineahead.entity.RestaurantTable;
import com.dineahead.entity.User;

import com.dineahead.enums.OrderStatus;
import com.dineahead.enums.PaymentStatus;
import com.dineahead.enums.ReservationStatus;

import com.dineahead.repository.OrderRepository;
import com.dineahead.repository.PaymentRepository;
import com.dineahead.repository.ReservationRepository;
import com.dineahead.repository.RestaurantRepository;
import com.dineahead.repository.RestaurantTableRepository;
import com.dineahead.repository.UserRepository;

import com.dineahead.service.OwnerDashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;


@Service
public class OwnerDashboardServiceImpl
        implements OwnerDashboardService {


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private RestaurantRepository restaurantRepository;


    @Autowired
    private RestaurantTableRepository restaurantTableRepository;


    @Autowired
    private ReservationRepository reservationRepository;


    @Autowired
    private OrderRepository orderRepository;


    @Autowired
    private PaymentRepository paymentRepository;


    @Override
    public OwnerDashboardResponseDTO
    getOwnerDashboard() {


        // ============================================
        // 1. Validate current user is a restaurant owner
        // ============================================

        if (!isRestaurantOwner()) {

            throw new AccessDeniedException(
                    "Only restaurant owners can access the owner dashboard."
            );
        }


        // ============================================
        // 2. Get authenticated owner
        // ============================================

        User currentOwner =
                getCurrentAuthenticatedUser();


        // ============================================
        // 3. Get owner's restaurants
        // ============================================

        List<Restaurant> restaurants =
                restaurantRepository.findAll()
                        .stream()
                        .filter(
                                restaurant ->
                                        restaurant.getOwner() != null
                                                &&
                                                restaurant
                                                        .getOwner()
                                                        .getId()
                                                        .equals(
                                                                currentOwner
                                                                        .getId()
                                                        )
                        )
                        .toList();


        Set<Long> restaurantIds =
                restaurants.stream()
                        .map(
                                Restaurant::getId
                        )
                        .collect(
                                java.util.stream.Collectors
                                        .toSet()
                        );


        // ============================================
        // 4. Get owner's tables
        // ============================================

        List<RestaurantTable> tables =
                restaurantTableRepository
                        .findAll()
                        .stream()
                        .filter(
                                table ->
                                        table.getRestaurant() != null
                                                &&
                                                restaurantIds.contains(
                                                        table
                                                                .getRestaurant()
                                                                .getId()
                                                )
                        )
                        .toList();


        // ============================================
        // 5. Get owner's reservations
        // ============================================

        List<Reservation> reservations =
                reservationRepository
                        .findAll()
                        .stream()
                        .filter(
                                reservation ->
                                        reservation
                                                .getRestaurant()
                                                != null
                                                &&
                                                restaurantIds.contains(
                                                        reservation
                                                                .getRestaurant()
                                                                .getId()
                                                )
                        )
                        .toList();


        // ============================================
        // 6. Get owner's orders
        // ============================================

        List<Order> orders =
                orderRepository
                        .findAll()
                        .stream()
                        .filter(
                                order ->
                                        order.getReservation()
                                                != null
                                                &&
                                                order
                                                        .getReservation()
                                                        .getRestaurant()
                                                        != null
                                                &&
                                                restaurantIds.contains(
                                                        order
                                                                .getReservation()
                                                                .getRestaurant()
                                                                .getId()
                                                )
                        )
                        .toList();


        // ============================================
        // 7. Get owner's completed payments
        // ============================================

        List<Payment> payments =
                paymentRepository
                        .findAll()
                        .stream()
                        .filter(
                                payment ->
                                        payment.getOrder()
                                                != null
                                                &&
                                                payment
                                                        .getOrder()
                                                        .getReservation()
                                                        != null
                                                &&
                                                payment
                                                        .getOrder()
                                                        .getReservation()
                                                        .getRestaurant()
                                                        != null
                                                &&
                                                restaurantIds.contains(
                                                        payment
                                                                .getOrder()
                                                                .getReservation()
                                                                .getRestaurant()
                                                                .getId()
                                                )
                        )
                        .filter(
                                payment ->
                                        payment.getStatus()
                                                == PaymentStatus
                                                .COMPLETED
                        )
                        .toList();


        // ============================================
        // 8. Create dashboard response
        // ============================================

        OwnerDashboardResponseDTO response =
                new OwnerDashboardResponseDTO();


        // Restaurants

        response.setTotalRestaurants(
                (long) restaurants.size()
        );


        // Tables

        response.setTotalTables(
                (long) tables.size()
        );


        // Reservations

        response.setTotalReservations(
                (long) reservations.size()
        );


        response.setConfirmedReservations(
                reservations.stream()
                        .filter(
                                reservation ->
                                        reservation.getStatus()
                                                == ReservationStatus
                                                .CONFIRMED
                        )
                        .count()
        );


        response.setCompletedReservations(
                reservations.stream()
                        .filter(
                                reservation ->
                                        reservation.getStatus()
                                                == ReservationStatus
                                                .COMPLETED
                        )
                        .count()
        );


        response.setCancelledReservations(
                reservations.stream()
                        .filter(
                                reservation ->
                                        reservation.getStatus()
                                                == ReservationStatus
                                                .CANCELLED
                        )
                        .count()
        );


        // Orders

        response.setTotalOrders(
                (long) orders.size()
        );


        response.setActiveOrders(
                orders.stream()
                        .filter(
                                order ->
                                        order.getStatus()
                                                == OrderStatus.PLACED
                                                ||
                                                order.getStatus()
                                                        == OrderStatus.ACCEPTED
                                                ||
                                                order.getStatus()
                                                        == OrderStatus.PREPARING
                                                ||
                                                order.getStatus()
                                                        == OrderStatus.READY
                        )
                        .count()
        );


        response.setCompletedOrders(
                orders.stream()
                        .filter(
                                order ->
                                        order.getStatus()
                                                == OrderStatus.SERVED
                        )
                        .count()
        );


        response.setCancelledOrders(
                orders.stream()
                        .filter(
                                order ->
                                        order.getStatus()
                                                == OrderStatus.CANCELLED
                        )
                        .count()
        );


        // Revenue

        BigDecimal totalRevenue =
                payments.stream()
                        .map(
                                Payment::getAmount
                        )
                        .filter(
                                amount ->
                                        amount != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        response.setTotalRevenue(
                totalRevenue
        );


        return response;
    }


    // ============================================
    // GET CURRENT AUTHENTICATED USER
    // ============================================

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
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Authenticated user not found."
                                )
                );
    }


    // ============================================
    // CHECK RESTAURANT OWNER ROLE
    // ============================================

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
                                authority
                                        .getAuthority()
                                        .equals(
                                                "ROLE_RESTAURANT_OWNER"
                                        )
                );
    }
}
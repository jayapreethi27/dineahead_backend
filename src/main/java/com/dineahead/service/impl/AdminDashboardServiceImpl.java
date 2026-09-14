package com.dineahead.service.impl;

import com.dineahead.dto.dashboard.AdminDashboardResponseDTO;

import com.dineahead.entity.Order;
import com.dineahead.entity.Payment;
import com.dineahead.entity.Reservation;
import com.dineahead.entity.Restaurant;
import com.dineahead.entity.User;

import com.dineahead.enums.OrderStatus;
import com.dineahead.enums.PaymentStatus;
import com.dineahead.enums.ReservationStatus;
import com.dineahead.enums.Role;

import com.dineahead.repository.OrderRepository;
import com.dineahead.repository.PaymentRepository;
import com.dineahead.repository.ReservationRepository;
import com.dineahead.repository.RestaurantRepository;
import com.dineahead.repository.UserRepository;

import com.dineahead.service.AdminDashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
public class AdminDashboardServiceImpl
        implements AdminDashboardService {


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private RestaurantRepository restaurantRepository;


    @Autowired
    private ReservationRepository reservationRepository;


    @Autowired
    private OrderRepository orderRepository;


    @Autowired
    private PaymentRepository paymentRepository;


    @Override
    public AdminDashboardResponseDTO
    getAdminDashboard() {


        // ============================================
        // 1. SECURITY CHECK
        // ============================================

        if (!isAdmin()) {

            throw new AccessDeniedException(
                    "Only administrators can access the admin dashboard."
            );
        }


        // ============================================
        // 2. LOAD DATA
        // ============================================

        List<User> users =
                userRepository.findAll();


        List<Restaurant> restaurants =
                restaurantRepository.findAll();


        List<Reservation> reservations =
                reservationRepository.findAll();


        List<Order> orders =
                orderRepository.findAll();


        List<Payment> payments =
                paymentRepository.findAll();


        // ============================================
        // 3. CREATE RESPONSE
        // ============================================

        AdminDashboardResponseDTO response =
                new AdminDashboardResponseDTO();


        // ============================================
        // RESTAURANTS
        // ============================================

        response.setTotalRestaurants(
                (long) restaurants.size()
        );


        response.setAssignedRestaurants(
                restaurants.stream()
                        .filter(
                                restaurant ->
                                        restaurant.getOwner()
                                                != null
                        )
                        .count()
        );


        response.setUnassignedRestaurants(
                restaurants.stream()
                        .filter(
                                restaurant ->
                                        restaurant.getOwner()
                                                == null
                        )
                        .count()
        );


        // ============================================
        // USERS
        // ============================================

        response.setTotalUsers(
                (long) users.size()
        );


        response.setTotalCustomers(
                users.stream()
                        .filter(
                                user ->
                                        user.getRole()
                                                == Role.CUSTOMER
                        )
                        .count()
        );


        response.setTotalRestaurantOwners(
                users.stream()
                        .filter(
                                user ->
                                        user.getRole()
                                                == Role.RESTAURANT_OWNER
                        )
                        .count()
        );


        response.setTotalAdmins(
                users.stream()
                        .filter(
                                user ->
                                        user.getRole()
                                                == Role.ADMIN
                        )
                        .count()
        );


        // ============================================
        // RESERVATIONS
        // ============================================

        response.setTotalReservations(
                (long) reservations.size()
        );


        response.setConfirmedReservations(
                reservations.stream()
                        .filter(
                                reservation ->
                                        reservation.getStatus()
                                                == ReservationStatus.CONFIRMED
                        )
                        .count()
        );


        response.setCompletedReservations(
                reservations.stream()
                        .filter(
                                reservation ->
                                        reservation.getStatus()
                                                == ReservationStatus.COMPLETED
                        )
                        .count()
        );


        response.setCancelledReservations(
                reservations.stream()
                        .filter(
                                reservation ->
                                        reservation.getStatus()
                                                == ReservationStatus.CANCELLED
                        )
                        .count()
        );


        // ============================================
        // ORDERS
        // ============================================

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


        response.setServedOrders(
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


        // ============================================
        // PAYMENTS
        // ============================================

        response.setTotalPayments(
                (long) payments.size()
        );


        response.setCompletedPayments(
                payments.stream()
                        .filter(
                                payment ->
                                        payment.getStatus()
                                                == PaymentStatus.COMPLETED
                        )
                        .count()
        );


        response.setPendingPayments(
                payments.stream()
                        .filter(
                                payment ->
                                        payment.getStatus()
                                                == PaymentStatus.PENDING
                        )
                        .count()
        );


        // ============================================
        // REVENUE
        //
        // Revenue is calculated only from
        // COMPLETED payments.
        // ============================================

        BigDecimal totalRevenue =
                payments.stream()
                        .filter(
                                payment ->
                                        payment.getStatus()
                                                == PaymentStatus.COMPLETED
                        )
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
    // CHECK ADMIN ROLE
    // ============================================

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
}
package com.dineahead.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminDashboardResponseDTO {

    // ============================================
    // RESTAURANTS
    // ============================================

    private Long totalRestaurants;

    private Long assignedRestaurants;

    private Long unassignedRestaurants;


    // ============================================
    // USERS
    // ============================================

    private Long totalUsers;

    private Long totalCustomers;

    private Long totalRestaurantOwners;

    private Long totalAdmins;


    // ============================================
    // RESERVATIONS
    // ============================================

    private Long totalReservations;

    private Long confirmedReservations;

    private Long completedReservations;

    private Long cancelledReservations;


    // ============================================
    // ORDERS
    // ============================================

    private Long totalOrders;

    private Long activeOrders;

    private Long servedOrders;

    private Long cancelledOrders;


    // ============================================
    // PAYMENTS
    // ============================================

    private Long totalPayments;

    private Long completedPayments;

    private Long pendingPayments;

    private BigDecimal totalRevenue;
}
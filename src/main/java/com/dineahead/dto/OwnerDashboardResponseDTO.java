package com.dineahead.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OwnerDashboardResponseDTO {

    private Long totalRestaurants;

    private Long totalTables;


    private Long totalReservations;

    private Long confirmedReservations;

    private Long completedReservations;

    private Long cancelledReservations;


    private Long totalOrders;

    private Long activeOrders;

    private Long completedOrders;

    private Long cancelledOrders;


    private BigDecimal totalRevenue;
}
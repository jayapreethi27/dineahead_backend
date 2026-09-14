package com.dineahead.service;

import com.dineahead.dto.RestaurantDashboardDTO;

public interface RestaurantDashboardService {

    RestaurantDashboardDTO getTodayDashboard(
            Long restaurantId
    );
}
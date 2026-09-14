package com.dineahead.controller;

import com.dineahead.dto.RestaurantDashboardDTO;
import com.dineahead.service.RestaurantDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantDashboardController {

    @Autowired
    private RestaurantDashboardService
            restaurantDashboardService;


    @GetMapping("/{restaurantId}/dashboard/today")
    public ResponseEntity<RestaurantDashboardDTO>
    getTodayDashboard(
            @PathVariable Long restaurantId) {

        RestaurantDashboardDTO response =
                restaurantDashboardService
                        .getTodayDashboard(
                                restaurantId
                        );

        return ResponseEntity.ok(response);
    }
}
package com.dineahead.controller;

import com.dineahead.dto.dashboard.OwnerDashboardResponseDTO;
import com.dineahead.service.OwnerDashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/dashboard/owner")
public class OwnerDashboardController {


    @Autowired
    private OwnerDashboardService
            ownerDashboardService;


    @GetMapping
    public ResponseEntity<OwnerDashboardResponseDTO>
    getOwnerDashboard() {

        OwnerDashboardResponseDTO response =
                ownerDashboardService
                        .getOwnerDashboard();


        return ResponseEntity.ok(
                response
        );
    }
}
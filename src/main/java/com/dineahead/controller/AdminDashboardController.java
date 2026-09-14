package com.dineahead.controller;

import com.dineahead.dto.dashboard.AdminDashboardResponseDTO;
import com.dineahead.service.AdminDashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/dashboard/admin")
public class AdminDashboardController {


    @Autowired
    private AdminDashboardService
            adminDashboardService;


    @GetMapping
    public ResponseEntity<AdminDashboardResponseDTO>
    getAdminDashboard() {

        AdminDashboardResponseDTO response =
                adminDashboardService
                        .getAdminDashboard();


        return ResponseEntity.ok(
                response
        );
    }
}
package com.dineahead.controller;

import com.dineahead.dto.LoginRequestDTO;
import com.dineahead.dto.LoginResponseDTO;

import com.dineahead.service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
public class AuthController {


    @Autowired
    private UserService userService;


    // ==========================================
    // LOGIN
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid
            @RequestBody
            LoginRequestDTO request
    ) {


        LoginResponseDTO response =
                userService.login(
                        request
                );


        return ResponseEntity.ok(
                response
        );
    }

}
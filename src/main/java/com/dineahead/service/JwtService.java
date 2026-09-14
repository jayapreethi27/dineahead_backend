package com.dineahead.service;

import com.dineahead.entity.User;

public interface JwtService {

    // Generate JWT token
    String generateToken(
            User user
    );


    // Extract email from JWT
    String extractUsername(
            String token
    );


    // Validate JWT token
    boolean validateToken(
            String token,
            String email
    );


    // Check whether token is expired
    boolean isTokenExpired(
            String token
    );

}
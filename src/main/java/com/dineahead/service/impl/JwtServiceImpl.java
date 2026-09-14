package com.dineahead.service.impl;

import com.dineahead.entity.User;
import com.dineahead.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;

import java.util.Date;


@Service
public class JwtServiceImpl
        implements JwtService {


    // ==========================================
    // JWT SECRET KEY
    // ==========================================
    private static final String JWT_SECRET =
            "DineAheadApplicationJwtSecretKeyForAuthentication2026";


    // ==========================================
    // TOKEN VALIDITY
    // 24 HOURS
    // ==========================================
    private static final long JWT_EXPIRATION =
            24 * 60 * 60 * 1000;


    // ==========================================
    // GET SIGNING KEY
    // ==========================================
    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                JWT_SECRET.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }


    // ==========================================
    // GENERATE TOKEN
    // ==========================================
    @Override
    public String generateToken(
            User user
    ) {

        Date issuedAt =
                new Date();

        Date expiration =
                new Date(
                        issuedAt.getTime()
                                + JWT_EXPIRATION
                );


        return Jwts.builder()

                // JWT subject
                .subject(
                        user.getEmail()
                )


                // Custom claims
                .claim(
                        "userId",
                        user.getId()
                )

                .claim(
                        "role",
                        user.getRole().name()
                )


                // Token dates
                .issuedAt(
                        issuedAt
                )

                .expiration(
                        expiration
                )


                // Sign token
                .signWith(
                        getSigningKey()
                )

                .compact();
    }


    // ==========================================
    // EXTRACT USER EMAIL
    // ==========================================
    @Override
    public String extractUsername(
            String token
    ) {

        return extractAllClaims(
                token
        ).getSubject();
    }


    // ==========================================
    // VALIDATE TOKEN
    // ==========================================
    @Override
    public boolean validateToken(
            String token,
            String email
    ) {

        String username =
                extractUsername(
                        token
                );


        return username.equals(
                email
        )

                &&

                !isTokenExpired(
                        token
                );
    }


    // ==========================================
    // CHECK TOKEN EXPIRATION
    // ==========================================
    @Override
    public boolean isTokenExpired(
            String token
    ) {

        Date expiration =
                extractAllClaims(
                        token
                )
                        .getExpiration();


        return expiration.before(
                new Date()
        );
    }


    // ==========================================
    // EXTRACT ALL CLAIMS
    // ==========================================
    private Claims extractAllClaims(
            String token
    ) {

        return Jwts.parser()

                .verifyWith(
                        getSigningKey()
                )

                .build()

                .parseSignedClaims(
                        token
                )

                .getPayload();
    }

}
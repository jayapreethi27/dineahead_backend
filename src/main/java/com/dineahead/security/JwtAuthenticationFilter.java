package com.dineahead.security;

import com.dineahead.service.CustomUserDetailsService;
import com.dineahead.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {


    @Autowired
    private JwtService jwtService;


    @Autowired
    private CustomUserDetailsService
            userDetailsService;


    @Override
    protected void doFilterInternal(

            HttpServletRequest request,

            HttpServletResponse response,

            FilterChain filterChain

    ) throws ServletException, IOException {


        // ======================================
        // READ AUTHORIZATION HEADER
        // ======================================
        String authorizationHeader =
                request.getHeader(
                        "Authorization"
                );


        // ======================================
        // CONTINUE IF JWT IS NOT PRESENT
        // ======================================
        if (

                authorizationHeader == null

                        ||

                        !authorizationHeader.startsWith(
                                "Bearer "
                        )

        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // ======================================
        // EXTRACT JWT TOKEN
        // ======================================
        String token =
                authorizationHeader.substring(
                        7
                );


        try {

            // ==================================
            // EXTRACT EMAIL FROM TOKEN
            // ==================================
            String email =
                    jwtService.extractUsername(
                            token
                    );


            // ==================================
            // AUTHENTICATE ONLY IF NOT ALREADY
            // AUTHENTICATED
            // ==================================
            if (

                    email != null

                            &&

                            SecurityContextHolder
                                    .getContext()
                                    .getAuthentication()
                                    == null

            ) {


                UserDetails userDetails =

                        userDetailsService
                                .loadUserByUsername(
                                        email
                                );


                // ==============================
                // VALIDATE JWT
                // ==============================
                if (

                        jwtService.validateToken(
                                token,
                                userDetails.getUsername()
                        )

                ) {


                    UsernamePasswordAuthenticationToken
                            authenticationToken =

                            new UsernamePasswordAuthenticationToken(

                                    userDetails,

                                    null,

                                    userDetails.getAuthorities()
                            );


                    authenticationToken.setDetails(

                            new WebAuthenticationDetailsSource()

                                    .buildDetails(
                                            request
                                    )
                    );


                    // ==========================
                    // SET AUTHENTICATION
                    // ==========================
                    SecurityContextHolder

                            .getContext()

                            .setAuthentication(
                                    authenticationToken
                            );
                }

            }

        } catch (Exception exception) {

            /*
             * Invalid or expired JWT.
             *
             * Do not authenticate.
             * SecurityConfig will decide whether
             * the request is allowed.
             */
        }


        // ======================================
        // CONTINUE FILTER CHAIN
        // ======================================
        filterChain.doFilter(
                request,
                response
        );
    }

}
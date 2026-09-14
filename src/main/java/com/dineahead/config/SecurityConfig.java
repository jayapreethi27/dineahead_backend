package com.dineahead.config;

import com.dineahead.security.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.security.config.Customizer;


@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Autowired
    private JwtAuthenticationFilter
            jwtAuthenticationFilter;


    // ==========================================
    // PASSWORD ENCODER
    // ==========================================
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }


    // ==========================================
    // SECURITY FILTER CHAIN
    // ==========================================
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {


        http
                // ==================================
                // ENABLE CORS
                // ==================================
                .cors(Customizer.withDefaults())

                // ==================================
                // DISABLE CSRF
                // ==================================
                .csrf(
                        csrf -> csrf.disable()
                )


                // ==================================
                // STATELESS JWT SECURITY
                // ==================================
                .sessionManagement(
                        session -> session
                                .sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )


                // ==================================
                // EXCEPTION HANDLING
                // ==================================
                .exceptionHandling(
                        exception -> exception

                                .authenticationEntryPoint(
                                        new HttpStatusEntryPoint(
                                                HttpStatus.UNAUTHORIZED
                                        )
                                )

                                .accessDeniedHandler(
                                        (request, response, accessDeniedException) -> {

                                            response.setStatus(
                                                    HttpStatus.FORBIDDEN.value()
                                            );

                                            response.setContentType(
                                                    "application/json"
                                            );

                                            response.getWriter().write(
                                                    """
                                                    {
                                                        "error": "Forbidden",
                                                        "message": "You do not have permission to access this resource."
                                                    }
                                                    """
                                            );
                                        }
                                )
                )


                // ==================================
                // AUTHORIZATION RULES
                // ==================================
                .authorizeHttpRequests(
                        auth -> auth

                                // ==========================
                                // AUTHENTICATION
                                // ==========================
                                .requestMatchers(
                                        "/api/auth/**"
                                )
                                .permitAll()


                                // ==========================
                                // USER REGISTRATION
                                // ==========================
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/users"
                                )
                                .permitAll()


                                // ==========================
                                // USER MANAGEMENT
                                // ADMIN ONLY
                                // ==========================
                                .requestMatchers(
                                        "/api/users/**"
                                )
                                .hasRole(
                                        "ADMIN"
                                )


                                // ==========================
                                // RESTAURANT VIEWING
                                // ANY AUTHENTICATED USER
                                // ==========================
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/restaurants/**"
                                )
                                .permitAll()


                                // ==========================
                                // CREATE RESTAURANT
                                // OWNER + ADMIN
                                // ==========================
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/restaurants/**"
                                )
                                .hasAnyRole(
                                        "RESTAURANT_OWNER",
                                        "ADMIN"
                                )

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/restaurants/*/owner/*"
                                )
                                .hasRole("ADMIN")


                                // ==========================
                                // UPDATE RESTAURANT
                                // OWNER + ADMIN
                                // ==========================
                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/restaurants/**"
                                )
                                .hasAnyRole(
                                        "RESTAURANT_OWNER",
                                        "ADMIN"
                                )


                                // ==========================
                                // DELETE RESTAURANT
                                // OWNER + ADMIN
                                // ==========================
                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/restaurants/**"
                                )
                                .hasAnyRole(
                                        "RESTAURANT_OWNER",
                                        "ADMIN"
                                )


                                // ==========================
                                // TABLE VIEWING
                                // ANY AUTHENTICATED USER
                                // ==========================
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/restaurant-tables/**"
                                )
                                .authenticated()


                                // ==========================
                                // TABLE MANAGEMENT
                                // OWNER + ADMIN
                                // ==========================
                                .requestMatchers(
                                        "/api/restaurant-tables/**"
                                )
                                .hasAnyRole(
                                        "RESTAURANT_OWNER",
                                        "ADMIN"
                                )


                                // ==========================
                                // MENU VIEWING
                                // ANY AUTHENTICATED USER
                                // ==========================
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/menu-items/**",
                                        "/api/menu-categories/**"
                                )
                                .permitAll()


                                // ==========================
                                // MENU MANAGEMENT
                                // OWNER + ADMIN
                                // ==========================
                                .requestMatchers(
                                        "/api/menu-items/**",
                                        "/api/menu-categories/**"
                                )
                                .hasAnyRole(
                                        "RESTAURANT_OWNER",
                                        "ADMIN"
                                )


                                // ==========================
                                // RESERVATIONS
                                // ==========================
                                .requestMatchers(
                                        "/api/reservations/**"
                                )
                                .authenticated()


                                // ==========================
                                // ORDERS
                                // ==========================
                                .requestMatchers(
                                        "/api/orders/**"
                                )
                                .authenticated()


                                // ==========================
                                // PAYMENTS
                                // ==========================
                                .requestMatchers(
                                        "/api/payments/**"
                                )
                                .authenticated()

                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**"
                                ).permitAll()


                                // ==========================
                                // EVERYTHING ELSE
                                // ==========================
                                .anyRequest()
                                .authenticated()
                )


                // ==================================
                // JWT FILTER
                // ==================================
                .addFilterBefore(

                        jwtAuthenticationFilter,

                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();

    }

}
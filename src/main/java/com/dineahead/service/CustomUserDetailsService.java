package com.dineahead.service;

import com.dineahead.entity.User;
import com.dineahead.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CustomUserDetailsService
        implements UserDetailsService {


    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {


        User user =
                userRepository.findByEmail(
                                email
                        )
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User with email "
                                                + email
                                                + " not found"
                                )
                        );


        return org.springframework.security.core.userdetails.User

                .withUsername(
                        user.getEmail()
                )

                .password(
                        user.getPassword()
                )

                .authorities(
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_"
                                                + user.getRole().name()
                                )
                        )
                )

                .build();
    }

}
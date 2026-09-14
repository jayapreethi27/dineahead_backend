package com.dineahead.service.impl;

import com.dineahead.dto.LoginRequestDTO;
import com.dineahead.dto.LoginResponseDTO;
import com.dineahead.dto.UserRequestDTO;
import com.dineahead.dto.UserResponseDTO;
import com.dineahead.entity.User;
import com.dineahead.enums.Role;
import com.dineahead.exception.UserNotFoundException;
import com.dineahead.repository.UserRepository;
import com.dineahead.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dineahead.dto.LoginRequestDTO;
import com.dineahead.dto.LoginResponseDTO;
import com.dineahead.service.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;


@Service
public class UserServiceImpl
        implements UserService {


    @Autowired
    private UserRepository userRepository;


    // ==========================================
    // ADDED FOR AUTHENTICATION
    // PASSWORD ENCODER
    // ==========================================
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================
// ADDED FOR JWT AUTHENTICATION
// ==========================================
    @Autowired
    private JwtService jwtService;


    @Override
    public UserResponseDTO saveUser(
            UserRequestDTO request) {


        // ==========================================
        // ADDED FOR AUTHENTICATION
        // CHECK DUPLICATE EMAIL
        // ==========================================
        if (userRepository.existsByEmail(
                request.getEmail()
        )) {

            throw new IllegalStateException(
                    "Email is already registered"
            );
        }


        User user = new User();

        user.setName(
                request.getName()
        );

        user.setEmail(
                request.getEmail()
        );

        user.setPhone(
                request.getPhone()
        );


        // ==========================================
        // ADDED FOR AUTHENTICATION
        // ENCRYPT PASSWORD USING BCrypt
        // ==========================================
        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );


        // ==========================================
        // ADDED FOR AUTHENTICATION
        // CONVERT STRING ROLE TO ENUM
        // ==========================================
        Role role;

        try {

            role = Role.valueOf(
                    request.getRole()
                            .toUpperCase()
            );

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Invalid role. Allowed roles are: "
                            + "CUSTOMER, "
                            + "RESTAURANT_OWNER, "
                            + "ADMIN"
            );
        }


        user.setRole(
                role
        );


        User savedUser =
                userRepository.save(
                        user
                );


        return mapToResponseDTO(
                savedUser
        );
    }


    @Override
    public UserResponseDTO getUserById(
            Long id) {


        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User with id "
                                                + id
                                                + " not found"
                                )
                        );


        return mapToResponseDTO(
                user
        );
    }


    @Override
    public UserResponseDTO updateUser(
            Long id,
            UserRequestDTO request) {


        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User with id "
                                                + id
                                                + " not found"
                                )
                        );


        user.setName(
                request.getName()
        );

        user.setEmail(
                request.getEmail()
        );

        user.setPhone(
                request.getPhone()
        );


        // ==========================================
        // ADDED FOR AUTHENTICATION
        // ENCRYPT UPDATED PASSWORD
        // ==========================================
        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );


        // ==========================================
        // ADDED FOR AUTHENTICATION
        // CONVERT STRING ROLE TO ENUM
        // ==========================================
        Role role;

        try {

            role = Role.valueOf(
                    request.getRole()
                            .toUpperCase()
            );

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Invalid role. Allowed roles are: "
                            + "CUSTOMER, "
                            + "RESTAURANT_OWNER, "
                            + "ADMIN"
            );
        }


        user.setRole(
                role
        );


        User updatedUser =
                userRepository.save(
                        user
                );


        return mapToResponseDTO(
                updatedUser
        );
    }


    @Override
    public void deleteUser(
            Long id) {


        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User with id "
                                                + id
                                                + " not found"
                                )
                        );


        userRepository.delete(
                user
        );
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        // ==========================================
        // FIND USER BY EMAIL
        // ==========================================
        User user =
                userRepository.findByEmail(
                                request.getEmail()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid email or password"
                                )
                        );


        // ==========================================
        // VERIFY BCrypt PASSWORD
        // ==========================================
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }


        // ==========================================
        // GENERATE JWT TOKEN
        // ==========================================
        String token =
                jwtService.generateToken(
                        user
                );


        // ==========================================
        // CREATE LOGIN RESPONSE
        // ==========================================
        LoginResponseDTO response =
                new LoginResponseDTO();


        response.setToken(
                token
        );

        response.setTokenType(
                "Bearer"
        );

        response.setUserId(
                user.getId()
        );

        response.setName(
                user.getName()
        );

        response.setEmail(
                user.getEmail()
        );

        response.setRole(
                user.getRole().name()
        );


        return response;
    }


    // ==========================================
    // ADDED FOR CLEAN RESPONSE MAPPING
    // ==========================================
    private UserResponseDTO mapToResponseDTO(
            User user) {


        UserResponseDTO response =
                new UserResponseDTO();


        response.setId(
                user.getId()
        );

        response.setName(
                user.getName()
        );

        response.setEmail(
                user.getEmail()
        );

        response.setPhone(
                user.getPhone()
        );


        // Convert Role enum back to String
        response.setRole(
                user.getRole().name()
        );


        return response;
    }

}
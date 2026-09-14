package com.dineahead.service;

import com.dineahead.dto.UserRequestDTO;
import com.dineahead.dto.UserResponseDTO;
import com.dineahead.entity.User;
import com.dineahead.dto.LoginRequestDTO;
import com.dineahead.dto.LoginResponseDTO;

public interface UserService {

    UserResponseDTO saveUser(UserRequestDTO request);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO updateUser(Long id, UserRequestDTO request);

    void deleteUser(Long id);

    LoginResponseDTO login(
            LoginRequestDTO request
    );
}

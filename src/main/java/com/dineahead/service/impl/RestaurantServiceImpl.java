package com.dineahead.service.impl;

import com.dineahead.dto.*;
import com.dineahead.entity.MenuCategory;
import com.dineahead.entity.MenuItem;
import com.dineahead.entity.Restaurant;
import com.dineahead.entity.RestaurantTable;
import com.dineahead.entity.User;
import com.dineahead.enums.Role;
import com.dineahead.exception.RestaurantNotFoundException;
import com.dineahead.repository.RestaurantRepository;
import com.dineahead.repository.UserRepository;
import com.dineahead.service.RestaurantService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl
        implements RestaurantService {


    private final RestaurantRepository
            restaurantRepository;


    // ==========================================
    // ADDED FOR RESTAURANT OWNERSHIP SECURITY
    // ==========================================
    private final UserRepository
            userRepository;


    @Override
    public RestaurantResponseDTO saveRestaurant(
            RestaurantRequestDTO request
    ) {

        // ==========================================
        // ADDED FOR RESTAURANT OWNERSHIP SECURITY
        // ==========================================
        User currentUser =
                getCurrentAuthenticatedUser();


        // ==========================================
        // ADDED FOR RESTAURANT OWNERSHIP SECURITY
        // ==========================================
        if (currentUser.getRole()
                != Role.RESTAURANT_OWNER
                && currentUser.getRole()
                != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Only restaurant owners or administrators "
                            + "can create restaurants."
            );
        }


        Restaurant restaurant =
                new Restaurant();

        restaurant.setName(
                request.getName()
        );

        restaurant.setAddress(
                request.getAddress()
        );

        restaurant.setPhone(
                request.getPhone()
        );

        restaurant.setEmail(
                request.getEmail()
        );


        // ==========================================
        // ADDED FOR RESTAURANT OWNERSHIP SECURITY
        // ==========================================
        restaurant.setOwner(
                currentUser
        );


        Restaurant savedRestaurant =
                restaurantRepository.save(
                        restaurant
                );


        return mapToResponseDTO(
                savedRestaurant
        );
    }


    @Override
    public RestaurantResponseDTO
    getRestaurantById(
            Long id
    ) {

        Restaurant restaurant =
                findRestaurantById(
                        id
                );


        return mapToResponseDTO(
                restaurant
        );
    }


    @Override
    public RestaurantResponseDTO
    updateRestaurant(
            Long id,
            RestaurantRequestDTO request
    ) {

        Restaurant restaurant =
                findRestaurantById(
                        id
                );


        // ==========================================
        // ADDED FOR RESTAURANT OWNERSHIP SECURITY
        // ==========================================
        validateRestaurantOwnership(
                restaurant
        );


        restaurant.setName(
                request.getName()
        );

        restaurant.setAddress(
                request.getAddress()
        );

        restaurant.setPhone(
                request.getPhone()
        );

        restaurant.setEmail(
                request.getEmail()
        );


        Restaurant updatedRestaurant =
                restaurantRepository.save(
                        restaurant
                );


        return mapToResponseDTO(
                updatedRestaurant
        );
    }

    @Override
    public RestaurantResponseDTO assignOwner(
            Long restaurantId,
            Long ownerId
    ) {

        Restaurant restaurant =
                restaurantRepository.findById(
                                restaurantId
                        )
                        .orElseThrow(() ->
                                new RestaurantNotFoundException(
                                        "Restaurant with id "
                                                + restaurantId
                                                + " not found"
                                )
                        );

        User owner =
                userRepository.findById(
                                ownerId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User with id "
                                                + ownerId
                                                + " not found"
                                )
                        );

        if (owner.getRole()
                != Role.RESTAURANT_OWNER) {

            throw new IllegalArgumentException(
                    "User with id "
                            + ownerId
                            + " is not a restaurant owner."
            );
        }

        restaurant.setOwner(owner);

        Restaurant savedRestaurant =
                restaurantRepository.save(
                        restaurant
                );

        return mapToResponseDTO(
                savedRestaurant
        );
    }


    @Override
    public void deleteRestaurant(
            Long id
    ) {

        Restaurant restaurant =
                findRestaurantById(
                        id
                );


        // ==========================================
        // ADDED FOR RESTAURANT OWNERSHIP SECURITY
        // ==========================================
        validateRestaurantOwnership(
                restaurant
        );


        restaurantRepository.delete(
                restaurant
        );
    }


    @Override
    public RestaurantWithTablesResponseDTO
    getRestaurantWithTables(
            Long id
    ) {

        Restaurant restaurant =
                findRestaurantById(
                        id
                );


        List<TableSummaryDTO> tableDTOs =
                new ArrayList<>();


        for (
                RestaurantTable table :
                restaurant.getRestaurantTables()
        ) {

            TableSummaryDTO tableDTO =
                    new TableSummaryDTO();

            tableDTO.setId(
                    table.getId()
            );

            tableDTO.setTableNumber(
                    table.getTableNumber()
            );

            tableDTO.setCapacity(
                    table.getCapacity()
            );

            tableDTOs.add(
                    tableDTO
            );
        }


        RestaurantWithTablesResponseDTO response =
                new RestaurantWithTablesResponseDTO();

        response.setId(
                restaurant.getId()
        );

        response.setName(
                restaurant.getName()
        );

        response.setAddress(
                restaurant.getAddress()
        );

        response.setPhone(
                restaurant.getPhone()
        );

        response.setEmail(
                restaurant.getEmail()
        );

        response.setTables(
                tableDTOs
        );


        return response;
    }


    @Override
    public List<RestaurantResponseDTO>
    getAllRestaurants() {

        List<Restaurant> restaurants =
                restaurantRepository.findAll();


        return restaurants.stream()
                .map(
                        this::mapToResponseDTO
                )
                .toList();
    }


    @Override
    public RestaurantMenuResponseDTO
    getRestaurantMenu(
            Long restaurantId
    ) {

        Restaurant restaurant =
                findRestaurantById(
                        restaurantId
                );


        List<RestaurantMenuCategoryDTO>
                categoryDTOs =
                new ArrayList<>();


        for (
                MenuCategory category :
                restaurant.getMenuCategories()
        ) {

            List<RestaurantMenuItemDTO>
                    itemDTOs =
                    new ArrayList<>();


            for (
                    MenuItem item :
                    category.getMenuItems()
            ) {

                RestaurantMenuItemDTO itemDTO =
                        new RestaurantMenuItemDTO();

                itemDTO.setId(
                        item.getId()
                );

                itemDTO.setName(
                        item.getName()
                );

                itemDTO.setDescription(
                        item.getDescription()
                );

                itemDTO.setPrice(
                        item.getPrice()
                );

                itemDTO.setAvailable(
                        item.getAvailable()
                );


                itemDTOs.add(
                        itemDTO
                );
            }


            RestaurantMenuCategoryDTO categoryDTO =
                    new RestaurantMenuCategoryDTO();

            categoryDTO.setId(
                    category.getId()
            );

            categoryDTO.setName(
                    category.getName()
            );

            categoryDTO.setItems(
                    itemDTOs
            );


            categoryDTOs.add(
                    categoryDTO
            );
        }


        RestaurantMenuResponseDTO response =
                new RestaurantMenuResponseDTO();

        response.setRestaurantId(
                restaurant.getId()
        );

        response.setRestaurantName(
                restaurant.getName()
        );

        response.setCategories(
                categoryDTOs
        );


        return response;
    }


    private Restaurant
    findRestaurantById(
            Long id
    ) {

        return restaurantRepository
                .findById(
                        id
                )
                .orElseThrow(
                        () ->
                                new RestaurantNotFoundException(
                                        "Restaurant with id "
                                                + id
                                                + " not found"
                                )
                );
    }


    private RestaurantResponseDTO
    mapToResponseDTO(
            Restaurant restaurant
    ) {

        RestaurantResponseDTO response =
                new RestaurantResponseDTO();

        response.setId(
                restaurant.getId()
        );

        response.setName(
                restaurant.getName()
        );

        response.setAddress(
                restaurant.getAddress()
        );

        response.setPhone(
                restaurant.getPhone()
        );

        response.setEmail(
                restaurant.getEmail()
        );


        return response;
    }


    // ==========================================
    // ADDED FOR RESTAURANT OWNERSHIP SECURITY
    // ==========================================
    private User
    getCurrentAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        String email =
                authentication.getName();


        return userRepository
                .findByEmail(
                        email
                )
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Authenticated user not found."
                                )
                );
    }


    // ==========================================
    // ADDED FOR RESTAURANT OWNERSHIP SECURITY
    // ==========================================
    private void
    validateRestaurantOwnership(
            Restaurant restaurant
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isAdmin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equals(
                                                        "ROLE_ADMIN"
                                                )
                        );


        // ADMIN can manage every restaurant
        if (isAdmin) {

            return;
        }


        User currentUser =
                getCurrentAuthenticatedUser();


        // Only restaurant owners can manage restaurants
        if (currentUser.getRole()
                != Role.RESTAURANT_OWNER) {

            throw new AccessDeniedException(
                    "Only restaurant owners can manage restaurants."
            );
        }


        // Restaurant must have an owner
        if (restaurant.getOwner()
                == null) {

            throw new AccessDeniedException(
                    "This restaurant does not have an assigned owner."
            );
        }


        // Owner can manage only their own restaurant
        if (!restaurant
                .getOwner()
                .getId()
                .equals(
                        currentUser.getId()
                )) {

            throw new AccessDeniedException(
                    "You do not have permission "
                            + "to manage this restaurant."
            );
        }
    }
}
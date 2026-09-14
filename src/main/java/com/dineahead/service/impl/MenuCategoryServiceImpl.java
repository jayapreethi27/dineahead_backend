package com.dineahead.service.impl;

import com.dineahead.dto.MenuCategoryRequestDTO;
import com.dineahead.dto.MenuCategoryResponseDTO;
import com.dineahead.entity.MenuCategory;
import com.dineahead.entity.Restaurant;
import com.dineahead.entity.User;
import com.dineahead.exception.RestaurantNotFoundException;
import com.dineahead.repository.MenuCategoryRepository;
import com.dineahead.repository.RestaurantRepository;
import com.dineahead.repository.UserRepository;
import com.dineahead.service.MenuCategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MenuCategoryServiceImpl
        implements MenuCategoryService {


    // ==========================================
    // REPOSITORIES
    // ==========================================

    @Autowired
    private MenuCategoryRepository
            menuCategoryRepository;


    @Autowired
    private RestaurantRepository
            restaurantRepository;


    @Autowired
    private UserRepository
            userRepository;


    // ==========================================
    // CREATE MENU CATEGORY
    // ==========================================

    @Override
    public MenuCategoryResponseDTO saveCategory(
            MenuCategoryRequestDTO request
    ) {

        // Find the restaurant
        Restaurant restaurant =
                restaurantRepository
                        .findById(
                                request.getRestaurantId()
                        )
                        .orElseThrow(() ->
                                new RestaurantNotFoundException(
                                        "Restaurant with id "
                                                + request.getRestaurantId()
                                                + " not found"
                                )
                        );


        // ==========================================
        // OWNERSHIP SECURITY
        // ==========================================

        validateRestaurantOwnership(
                restaurant
        );


        // Create category
        MenuCategory category =
                new MenuCategory();


        category.setName(
                request.getName()
        );


        category.setRestaurant(
                restaurant
        );


        // Save category
        MenuCategory savedCategory =
                menuCategoryRepository
                        .save(
                                category
                        );


        return mapToResponseDTO(
                savedCategory
        );
    }


    // ==========================================
    // GET CATEGORIES BY RESTAURANT
    // ==========================================

    @Override
    public List<MenuCategoryResponseDTO>
    getCategoriesByRestaurantId(
            Long restaurantId
    ) {

        return menuCategoryRepository
                .findByRestaurantId(
                        restaurantId
                )
                .stream()
                .map(
                        this::mapToResponseDTO
                )
                .toList();
    }


    // ==========================================
    // UPDATE MENU CATEGORY
    // ==========================================

    @Override
    public MenuCategoryResponseDTO updateCategory(
            Long id,
            MenuCategoryRequestDTO request
    ) {

        // Find category
        MenuCategory category =
                findCategoryById(
                        id
                );


        // ==========================================
        // OWNERSHIP SECURITY
        // ==========================================

        validateRestaurantOwnership(
                category.getRestaurant()
        );


        /*
         * We only update the category name.
         *
         * The category remains associated with
         * its existing restaurant.
         */
        category.setName(
                request.getName()
        );


        // Save updated category
        MenuCategory updatedCategory =
                menuCategoryRepository
                        .save(
                                category
                        );


        return mapToResponseDTO(
                updatedCategory
        );
    }


    // ==========================================
    // DELETE MENU CATEGORY
    // ==========================================

    @Override
    public void deleteCategory(
            Long id
    ) {

        // Find category
        MenuCategory category =
                findCategoryById(
                        id
                );


        // ==========================================
        // OWNERSHIP SECURITY
        // ==========================================

        validateRestaurantOwnership(
                category.getRestaurant()
        );


        // Delete category
        menuCategoryRepository
                .delete(
                        category
                );
    }


    // ==========================================
    // FIND CATEGORY BY ID
    // ==========================================

    private MenuCategory findCategoryById(
            Long id
    ) {

        return menuCategoryRepository
                .findById(
                        id
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Menu category with id "
                                        + id
                                        + " not found"
                        )
                );
    }


    // ==========================================
    // MAP ENTITY TO RESPONSE DTO
    // ==========================================

    private MenuCategoryResponseDTO
    mapToResponseDTO(
            MenuCategory category
    ) {

        MenuCategoryResponseDTO response =
                new MenuCategoryResponseDTO();


        response.setId(
                category.getId()
        );


        response.setName(
                category.getName()
        );


        response.setRestaurantId(
                category
                        .getRestaurant()
                        .getId()
        );


        return response;
    }


    // ==========================================
    // GET CURRENT AUTHENTICATED USER
    // ==========================================

    private User getCurrentAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        String email =
                authentication
                        .getName();


        return userRepository
                .findByEmail(
                        email
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found."
                        )
                );
    }


    // ==========================================
    // VALIDATE RESTAURANT OWNERSHIP
    // ==========================================

    private void validateRestaurantOwnership(
            Restaurant restaurant
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        // ==========================================
        // CHECK IF CURRENT USER IS ADMIN
        // ==========================================

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


        // ==========================================
        // ADMIN CAN MANAGE ALL CATEGORIES
        // ==========================================

        if (isAdmin) {

            return;
        }


        // ==========================================
        // GET CURRENT LOGGED-IN USER
        // ==========================================

        User currentUser =
                getCurrentAuthenticatedUser();


        // ==========================================
        // VERIFY RESTAURANT OWNER
        // ==========================================

        if (
                restaurant.getOwner() == null
                        ||
                        !restaurant
                                .getOwner()
                                .getId()
                                .equals(
                                        currentUser.getId()
                                )
        ) {

            throw new AccessDeniedException(
                    "You do not have permission to manage "
                            + "menu categories for this restaurant."
            );
        }
    }
}
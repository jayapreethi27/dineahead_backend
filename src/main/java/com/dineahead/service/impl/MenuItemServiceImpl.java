package com.dineahead.service.impl;

import com.dineahead.dto.MenuItemRequestDTO;
import com.dineahead.dto.MenuItemResponseDTO;

import com.dineahead.entity.MenuCategory;
import com.dineahead.entity.MenuItem;
import com.dineahead.entity.Restaurant;
import com.dineahead.entity.User;

import com.dineahead.exception.MenuCategoryNotFoundException;

import com.dineahead.repository.MenuCategoryRepository;
import com.dineahead.repository.MenuItemRepository;
import com.dineahead.repository.UserRepository;

import com.dineahead.service.MenuItemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MenuItemServiceImpl
        implements MenuItemService {


    // ==========================================
    // REPOSITORIES
    // ==========================================

    @Autowired
    private MenuItemRepository
            menuItemRepository;


    @Autowired
    private MenuCategoryRepository
            menuCategoryRepository;


    // ==========================================
    // ADDED: USER REPOSITORY
    // ==========================================

    @Autowired
    private UserRepository
            userRepository;


    // ==========================================
    // CREATE MENU ITEM
    // ==========================================

    @Override
    public MenuItemResponseDTO
    saveMenuItem(
            MenuItemRequestDTO request
    ) {

        // Find category
        MenuCategory menuCategory =
                findCategoryById(
                        request.getCategoryId()
                );


        // ==========================================
        // ADDED: OWNERSHIP SECURITY
        // ==========================================

        validateRestaurantOwnership(
                menuCategory
                        .getRestaurant()
        );


        // Create menu item
        MenuItem menuItem =
                new MenuItem();


        menuItem.setName(
                request.getName()
        );


        menuItem.setDescription(
                request.getDescription()
        );


        menuItem.setPrice(
                request.getPrice()
        );


        menuItem.setAvailable(
                request.getAvailable()
        );


        menuItem.setCategory(
                menuCategory
        );


        // Save
        MenuItem savedMenuItem =
                menuItemRepository
                        .save(
                                menuItem
                        );


        return mapToResponseDTO(
                savedMenuItem
        );
    }


    // ==========================================
    // GET MENU ITEMS BY CATEGORY
    // ==========================================

    @Override
    public List<MenuItemResponseDTO>
    getMenuItemsByCategoryId(
            Long categoryId
    ) {

        return menuItemRepository
                .findByCategoryId(
                        categoryId
                )
                .stream()
                .map(
                        this::mapToResponseDTO
                )
                .toList();
    }


    // ==========================================
    // ADDED: UPDATE MENU ITEM
    // ==========================================

    @Override
    public MenuItemResponseDTO
    updateMenuItem(
            Long id,
            MenuItemRequestDTO request
    ) {

        // Find existing menu item
        MenuItem menuItem =
                findMenuItemById(
                        id
                );


        /*
         * IMPORTANT:
         *
         * Validate ownership using the EXISTING
         * menu item's restaurant.
         *
         * This prevents Owner A from changing
         * another owner's menu item.
         */
        validateRestaurantOwnership(
                menuItem
                        .getCategory()
                        .getRestaurant()
        );


        /*
         * Find the requested category.
         *
         * We allow moving an item between categories,
         * but only if the new category belongs to
         * a restaurant owned by the same user.
         */
        MenuCategory newCategory =
                findCategoryById(
                        request.getCategoryId()
                );


        // ==========================================
        // ADDED: VALIDATE NEW CATEGORY OWNERSHIP
        // ==========================================

        validateRestaurantOwnership(
                newCategory
                        .getRestaurant()
        );


        // Update fields
        menuItem.setName(
                request.getName()
        );


        menuItem.setDescription(
                request.getDescription()
        );


        menuItem.setPrice(
                request.getPrice()
        );


        menuItem.setAvailable(
                request.getAvailable()
        );


        menuItem.setCategory(
                newCategory
        );


        MenuItem updatedMenuItem =
                menuItemRepository
                        .save(
                                menuItem
                        );


        return mapToResponseDTO(
                updatedMenuItem
        );
    }


    // ==========================================
    // ADDED: DELETE MENU ITEM
    // ==========================================

    @Override
    public void deleteMenuItem(
            Long id
    ) {

        // Find item
        MenuItem menuItem =
                findMenuItemById(
                        id
                );


        // ==========================================
        // ADDED: OWNERSHIP SECURITY
        // ==========================================

        validateRestaurantOwnership(
                menuItem
                        .getCategory()
                        .getRestaurant()
        );


        // Delete
        menuItemRepository
                .delete(
                        menuItem
                );
    }


    // ==========================================
    // FIND MENU ITEM
    // ==========================================

    private MenuItem findMenuItemById(
            Long id
    ) {

        return menuItemRepository
                .findById(
                        id
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Menu item with id "
                                        + id
                                        + " not found"
                        )
                );
    }


    // ==========================================
    // FIND CATEGORY
    // ==========================================

    private MenuCategory findCategoryById(
            Long categoryId
    ) {

        return menuCategoryRepository
                .findById(
                        categoryId
                )
                .orElseThrow(() ->
                        new MenuCategoryNotFoundException(
                                "Menu category with id "
                                        + categoryId
                                        + " not found"
                        )
                );
    }


    // ==========================================
    // MAP TO RESPONSE DTO
    // ==========================================

    private MenuItemResponseDTO
    mapToResponseDTO(
            MenuItem menuItem
    ) {

        MenuItemResponseDTO response =
                new MenuItemResponseDTO();


        response.setId(
                menuItem.getId()
        );


        response.setName(
                menuItem.getName()
        );


        response.setDescription(
                menuItem.getDescription()
        );


        response.setPrice(
                menuItem.getPrice()
        );


        response.setAvailable(
                menuItem.getAvailable()
        );


        response.setCategoryId(
                menuItem
                        .getCategory()
                        .getId()
        );


        return response;
    }


    // ==========================================
    // ADDED: GET CURRENT AUTHENTICATED USER
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
    // ADDED: VALIDATE RESTAURANT OWNERSHIP
    // ==========================================

    private void validateRestaurantOwnership(
            Restaurant restaurant
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        // ==========================================
        // ADMIN CHECK
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


        // ADMIN CAN MANAGE EVERYTHING
        if (isAdmin) {

            return;
        }


        // ==========================================
        // GET CURRENT USER
        // ==========================================

        User currentUser =
                getCurrentAuthenticatedUser();


        // ==========================================
        // CHECK RESTAURANT OWNER
        // ==========================================

        if (
                restaurant.getOwner() == null
                        ||
                        !restaurant
                                .getOwner()
                                .getId()
                                .equals(
                                        currentUser
                                                .getId()
                                )
        ) {

            throw new AccessDeniedException(
                    "You do not have permission "
                            + "to manage menu items "
                            + "for this restaurant."
            );
        }
    }
}
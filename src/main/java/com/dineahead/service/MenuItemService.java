package com.dineahead.service;

import com.dineahead.dto.MenuItemRequestDTO;
import com.dineahead.dto.MenuItemResponseDTO;

import java.util.List;

public interface MenuItemService {

    MenuItemResponseDTO saveMenuItem(
            MenuItemRequestDTO request
    );

    List<MenuItemResponseDTO>
    getMenuItemsByCategoryId(
            Long categoryId
    );


    // ==========================================
    // ADDED: UPDATE MENU ITEM
    // ==========================================
    MenuItemResponseDTO updateMenuItem(
            Long id,
            MenuItemRequestDTO request
    );


    // ==========================================
    // ADDED: DELETE MENU ITEM
    // ==========================================
    void deleteMenuItem(
            Long id
    );
}
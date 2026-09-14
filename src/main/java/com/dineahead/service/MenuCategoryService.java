package com.dineahead.service;

import com.dineahead.dto.MenuCategoryRequestDTO;
import com.dineahead.dto.MenuCategoryResponseDTO;

import java.util.List;

public interface MenuCategoryService {

    MenuCategoryResponseDTO saveCategory(
            MenuCategoryRequestDTO request
    );
    List<MenuCategoryResponseDTO> getCategoriesByRestaurantId(
            Long restaurantId
    );

    MenuCategoryResponseDTO updateCategory(
            Long id,
            MenuCategoryRequestDTO request
    );

    void deleteCategory(
            Long id
    );
}

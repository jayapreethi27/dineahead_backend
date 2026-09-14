package com.dineahead.controller;

import com.dineahead.dto.MenuCategoryRequestDTO;
import com.dineahead.dto.MenuCategoryResponseDTO;
import com.dineahead.service.MenuCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu-categories")
public class MenuCategoryController {

    @Autowired
    private MenuCategoryService menuCategoryService;

    @PostMapping
    public ResponseEntity<MenuCategoryResponseDTO> saveCategory(
            @Valid @RequestBody MenuCategoryRequestDTO request) {

        MenuCategoryResponseDTO response =
                menuCategoryService.saveCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuCategoryResponseDTO>>
    getCategoriesByRestaurantId(
            @PathVariable Long restaurantId) {

        List<MenuCategoryResponseDTO> response =
                menuCategoryService
                        .getCategoriesByRestaurantId(
                                restaurantId
                        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuCategoryResponseDTO>
    updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody
            MenuCategoryRequestDTO request
    ) {

        MenuCategoryResponseDTO response =
                menuCategoryService.updateCategory(
                        id,
                        request
                );

        return ResponseEntity.ok(
                response
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteCategory(
            @PathVariable Long id
    ) {

        menuCategoryService.deleteCategory(
                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}
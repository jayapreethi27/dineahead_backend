package com.dineahead.controller;

import com.dineahead.dto.MenuItemRequestDTO;
import com.dineahead.dto.MenuItemResponseDTO;
import com.dineahead.service.MenuItemService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {


    @Autowired
    private MenuItemService menuItemService;


    // ==========================================
    // CREATE MENU ITEM
    // ==========================================

    @PostMapping
    public ResponseEntity<MenuItemResponseDTO>
    saveMenuItem(
            @Valid
            @RequestBody
            MenuItemRequestDTO request
    ) {

        MenuItemResponseDTO response =
                menuItemService
                        .saveMenuItem(
                                request
                        );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        response
                );
    }


    // ==========================================
    // GET MENU ITEMS BY CATEGORY
    // ==========================================

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<MenuItemResponseDTO>>
    getMenuItemsByCategoryId(
            @PathVariable
            Long categoryId
    ) {

        List<MenuItemResponseDTO> response =
                menuItemService
                        .getMenuItemsByCategoryId(
                                categoryId
                        );

        return ResponseEntity
                .ok(
                        response
                );
    }


    // ==========================================
    // ADDED: UPDATE MENU ITEM
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponseDTO>
    updateMenuItem(
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            MenuItemRequestDTO request
    ) {

        MenuItemResponseDTO response =
                menuItemService
                        .updateMenuItem(
                                id,
                                request
                        );

        return ResponseEntity
                .ok(
                        response
                );
    }


    // ==========================================
    // ADDED: DELETE MENU ITEM
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteMenuItem(
            @PathVariable
            Long id
    ) {

        menuItemService
                .deleteMenuItem(
                        id
                );

        return ResponseEntity
                .noContent()
                .build();
    }
}
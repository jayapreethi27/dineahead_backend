package com.dineahead.controller;

import com.dineahead.dto.*;
import com.dineahead.service.RestaurantService;
import com.dineahead.service.RestaurantTableService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private RestaurantTableService restaurantTableService;

    @PostMapping
    public ResponseEntity<RestaurantResponseDTO> saveRestaurant(
            @Valid @RequestBody RestaurantRequestDTO request) {

        RestaurantResponseDTO response = restaurantService.saveRestaurant(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDTO> getRestaurantById(
            @PathVariable Long id){

        RestaurantResponseDTO response = restaurantService.getRestaurantById(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{restaurantId}/owner/{ownerId}")
    public ResponseEntity<RestaurantResponseDTO> assignOwner(
            @PathVariable Long restaurantId,
            @PathVariable Long ownerId
    ) {

        RestaurantResponseDTO response =
                restaurantService.assignOwner(
                        restaurantId,
                        ownerId
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponseDTO> updateRestaurant(
                @PathVariable Long id,
                @Valid @RequestBody RestaurantRequestDTO request){

        RestaurantResponseDTO response = restaurantService.updateRestaurant(id, request);

        return  ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id){

        restaurantService.deleteRestaurant(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{restaurantId}/tables")
    public ResponseEntity<List<RestaurantTableResponseDTO>> getTablesByRestaurantId(
            @PathVariable Long restaurantId){

        List<RestaurantTableResponseDTO> response = restaurantTableService.getTablesByRestaurantId(restaurantId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<RestaurantWithTablesResponseDTO> getRestaurnatWithTables(@PathVariable Long id){

        RestaurantWithTablesResponseDTO response = restaurantService.getRestaurantWithTables(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponseDTO>> getAllRestaurants() {
        List<RestaurantResponseDTO> response =
                restaurantService.getAllRestaurants();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<RestaurantMenuResponseDTO> getRestaurantMenu(
            @PathVariable Long id) {

        RestaurantMenuResponseDTO response =
                restaurantService.getRestaurantMenu(id);

        return ResponseEntity.ok(response);
    }


}

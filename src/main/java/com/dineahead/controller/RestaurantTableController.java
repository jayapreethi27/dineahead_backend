package com.dineahead.controller;

import com.dineahead.dto.RestaurantTableRequestDTO;
import com.dineahead.dto.RestaurantTableResponseDTO;
import com.dineahead.service.RestaurantTableService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

//import java.util.List;

@RestController
@RequestMapping("/api/restaurant-tables")
public class RestaurantTableController {

    @Autowired
    private RestaurantTableService restaurantTableService;

    @PostMapping
    public ResponseEntity<RestaurantTableResponseDTO> saveRestaurantTable(
            @Valid @RequestBody RestaurantTableRequestDTO request){

        RestaurantTableResponseDTO response = restaurantTableService.saveRestaurantTable(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantTableResponseDTO> getRestaurantTableById(
            @PathVariable Long id){

        RestaurantTableResponseDTO response = restaurantTableService.getRestaurantTableById(id);

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/restaurants/{restaurantId}/tables")
//    public ResponseEntity<List<RestaurantTableResponseDTO>> getTablesByRestaurantId(
//            @PathVariable Long restaurantId) {
//
//        List<RestaurantTableResponseDTO> response = restaurantTableService.getTablesByRestaurantId(restaurantId);
//
//        return ResponseEntity.ok(response);
//    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantTableResponseDTO> updateRestaurantTable(
            @PathVariable Long id, @Valid @RequestBody RestaurantTableRequestDTO request){

        RestaurantTableResponseDTO response = restaurantTableService.updatedRestaurantTable(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurantTable(@PathVariable Long id){
        restaurantTableService.deleteRestaurantTable(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public List<RestaurantTableResponseDTO> getAvailableTables(
            @RequestParam Long restaurantId,
            @RequestParam LocalDateTime reservationTime,
            @RequestParam Integer partySize){

        return restaurantTableService.getAvailableTables(
                restaurantId,
                reservationTime,
                partySize
        );
    }
}

package com.dineahead.controller;

import com.dineahead.dto.CustomerHistoryResponseDTO;
import com.dineahead.dto.ReservationRequestDTO;
import com.dineahead.dto.ReservationResponseDTO;
import com.dineahead.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponseDTO>
    saveReservation(
            @Valid
            @RequestBody
            ReservationRequestDTO request){

        ReservationResponseDTO response = reservationService.saveReservation(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> getReservationById(@PathVariable Long id){

        ReservationResponseDTO response = reservationService.getReservationById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations(){

        List<ReservationResponseDTO> responses = reservationService.getAllReservations();

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> updateReservation(
            @PathVariable Long id,
            @Valid
            @RequestBody ReservationRequestDTO request){

        ReservationResponseDTO response = reservationService.updateReservation(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id){

        reservationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity <List<ReservationResponseDTO>> getReservationsByRestaurantId(
            @PathVariable Long restaurantId) {

        List<ReservationResponseDTO> response = reservationService.getReservationsByRestaurantId(
                restaurantId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer")
    public ResponseEntity<List<ReservationResponseDTO>> getReservationsByCustomerName(
            @RequestParam String customerName) {

        List<ReservationResponseDTO> response =
                reservationService.getReservationsByCustomerName(
                        customerName
                );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id) {

        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> completeReservation(@PathVariable Long id) {

        reservationService.completeReservation(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/customer/{customerName}/history")
    public ResponseEntity<CustomerHistoryResponseDTO>
    getCustomerHistory(
            @PathVariable String customerName) {

        CustomerHistoryResponseDTO response =
                reservationService.getCustomerHistory(
                        customerName
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurant/{restaurantId}/today")
    public ResponseEntity<List<ReservationResponseDTO>>
    getTodaysReservationsByRestaurantId(
            @PathVariable Long restaurantId) {

        List<ReservationResponseDTO> response =
                reservationService
                        .getTodaysReservationsByRestaurantId(
                                restaurantId
                        );

        return ResponseEntity.ok(response);
    }
}

package com.dineahead.controller;

import com.dineahead.dto.OrderRequestDTO;
import com.dineahead.dto.OrderResponseDTO;
import com.dineahead.enums.OrderStatus;
import com.dineahead.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO request) {

        OrderResponseDTO response =
                orderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        OrderResponseDTO response =
                orderService.updateOrderStatus(
                        orderId,
                        status
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {

        List<OrderResponseDTO> response =
                orderService.getAllOrders();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/kitchen")
    public ResponseEntity<List<OrderResponseDTO>>
    getKitchenOrders() {

        List<OrderResponseDTO> response =
                orderService.getKitchenOrders();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable Long id) {

        OrderResponseDTO response =
                orderService.getOrderById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<OrderResponseDTO>>
    getOrdersByReservationId(
            @PathVariable Long reservationId) {

        List<OrderResponseDTO> response =
                orderService.getOrdersByReservationId(
                        reservationId
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @PathVariable Long orderId) {

        OrderResponseDTO response =
                orderService.cancelOrder(orderId);

        return ResponseEntity.ok(response);
    }
}
package com.dineahead.controller;

import com.dineahead.dto.PaymentRequestDTO;
import com.dineahead.dto.PaymentResponseDTO;
import com.dineahead.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @Valid @RequestBody PaymentRequestDTO request) {

        PaymentResponseDTO response =
                paymentService.createPayment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDTO>
    getPaymentByOrderId(
            @PathVariable Long orderId) {

        PaymentResponseDTO response =
                paymentService.getPaymentByOrderId(
                        orderId
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{paymentId}/complete")
    public ResponseEntity<PaymentResponseDTO>
    completePayment(
            @PathVariable Long paymentId) {

        PaymentResponseDTO response =
                paymentService.completePayment(
                        paymentId
                );

        return ResponseEntity.ok(response);
    }


}
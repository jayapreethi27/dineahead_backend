package com.dineahead.dto;

import com.dineahead.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private Long reservationId;

    private String customerName;
    private LocalDateTime reservationTime;

    private Long restaurantId;
    private Long tableId;
    private Integer tableNumber;

    private OrderStatus status;
    private BigDecimal totalAmount;

    private List<OrderItemResponseDTO> items;
}
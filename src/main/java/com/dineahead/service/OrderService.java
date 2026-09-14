package com.dineahead.service;

import com.dineahead.dto.OrderRequestDTO;
import com.dineahead.dto.OrderResponseDTO;
import com.dineahead.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponseDTO createOrder(OrderRequestDTO request);

    OrderResponseDTO updateOrderStatus(
            Long orderId,
            OrderStatus status
    );

    OrderResponseDTO cancelOrder(Long orderId);

    List<OrderResponseDTO> getAllOrders();

    OrderResponseDTO getOrderById(Long id);

    List<OrderResponseDTO> getOrdersByReservationId(Long reservationId);

    List<OrderResponseDTO> getKitchenOrders();
}

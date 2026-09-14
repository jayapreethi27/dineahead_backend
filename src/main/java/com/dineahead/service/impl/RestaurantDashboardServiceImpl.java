package com.dineahead.service.impl;

import com.dineahead.dto.OrderItemResponseDTO;
import com.dineahead.dto.OrderResponseDTO;
import com.dineahead.dto.PaymentResponseDTO;
import com.dineahead.dto.ReservationResponseDTO;
import com.dineahead.dto.RestaurantDashboardDTO;
import com.dineahead.dto.RestaurantOrderDashboardDTO;
import com.dineahead.entity.Order;
import com.dineahead.entity.OrderItem;
import com.dineahead.entity.Payment;
import com.dineahead.entity.Reservation;
import com.dineahead.repository.OrderRepository;
import com.dineahead.repository.PaymentRepository;
import com.dineahead.repository.ReservationRepository;
import com.dineahead.service.RestaurantDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantDashboardServiceImpl
        implements RestaurantDashboardService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;


    @Override
    public RestaurantDashboardDTO getTodayDashboard(
            Long restaurantId) {

        LocalDateTime startOfDay =
                LocalDateTime.now()
                        .toLocalDate()
                        .atStartOfDay();

        LocalDateTime endOfDay =
                startOfDay.plusDays(1);


        // Today's reservations
        List<Reservation> reservations =
                reservationRepository
                        .findByRestaurantIdAndReservationTimeBetween(
                                restaurantId,
                                startOfDay,
                                endOfDay
                        );


        List<ReservationResponseDTO>
                reservationResponses =
                reservations.stream()
                        .map(this::mapReservationToResponseDTO)
                        .toList();


        // Today's orders
        List<Order> orders =
                orderRepository
                        .findByReservationRestaurantIdAndReservationReservationTimeBetween(
                                restaurantId,
                                startOfDay,
                                endOfDay
                        );


        List<RestaurantOrderDashboardDTO>
                orderResponses =
                new ArrayList<>();


        for (Order order : orders) {

            OrderResponseDTO orderResponse =
                    mapOrderToResponseDTO(order);

            PaymentResponseDTO paymentResponse =
                    null;

            Payment payment =
                    paymentRepository
                            .findByOrderId(order.getId())
                            .orElse(null);

            if (payment != null) {

                paymentResponse =
                        mapPaymentToResponseDTO(
                                payment
                        );
            }

            RestaurantOrderDashboardDTO
                    dashboardOrder =
                    new RestaurantOrderDashboardDTO();

            dashboardOrder.setOrder(
                    orderResponse
            );

            dashboardOrder.setPayment(
                    paymentResponse
            );

            orderResponses.add(
                    dashboardOrder
            );
        }


        RestaurantDashboardDTO response =
                new RestaurantDashboardDTO();

        response.setRestaurantId(
                restaurantId
        );

        response.setReservations(
                reservationResponses
        );

        response.setOrders(
                orderResponses
        );

        return response;
    }


    private ReservationResponseDTO
    mapReservationToResponseDTO(
            Reservation reservation) {

        ReservationResponseDTO response =
                new ReservationResponseDTO();

        response.setId(
                reservation.getId()
        );

        response.setCustomerName(
                reservation.getCustomerName()
        );

        response.setReservationTime(
                reservation.getReservationTime()
        );

        response.setPartySize(
                reservation.getPartySize()
        );

        response.setRestaurantId(
                reservation.getRestaurant()
                        .getId()
        );

        response.setRestaurantTableId(
                reservation.getRestaurantTable()
                        .getId()
        );

        response.setStatus(
                reservation.getStatus()
        );

        return response;
    }


    private OrderResponseDTO
    mapOrderToResponseDTO(
            Order order) {

        OrderResponseDTO response =
                new OrderResponseDTO();

        response.setId(
                order.getId()
        );

        response.setReservationId(
                order.getReservation()
                        .getId()
        );

        response.setCustomerName(
                order.getReservation()
                        .getCustomerName()
        );

        response.setReservationTime(
                order.getReservation()
                        .getReservationTime()
        );

        response.setRestaurantId(
                order.getReservation()
                        .getRestaurant()
                        .getId()
        );

        response.setTableId(
                order.getReservation()
                        .getRestaurantTable()
                        .getId()
        );

        response.setTableNumber(
                order.getReservation()
                        .getRestaurantTable()
                        .getTableNumber()
        );

        response.setStatus(
                order.getStatus()
        );

        response.setTotalAmount(
                order.getTotalAmount()
        );


        List<OrderItemResponseDTO> items =
                new ArrayList<>();

        for (OrderItem item :
                order.getOrderItems()) {

            OrderItemResponseDTO itemResponse =
                    new OrderItemResponseDTO();

            itemResponse.setMenuItemId(
                    item.getMenuItem().getId()
            );

            itemResponse.setMenuItemName(
                    item.getMenuItem().getName()
            );

            itemResponse.setQuantity(
                    item.getQuantity()
            );

            itemResponse.setPrice(
                    item.getPrice()
            );

            BigDecimal subtotal =
                    item.getPrice().multiply(
                            BigDecimal.valueOf(
                                    item.getQuantity()
                            )
                    );

            itemResponse.setSubtotal(
                    subtotal
            );

            items.add(itemResponse);
        }

        response.setItems(items);

        return response;
    }


    private PaymentResponseDTO
    mapPaymentToResponseDTO(
            Payment payment) {

        PaymentResponseDTO response =
                new PaymentResponseDTO();

        response.setId(
                payment.getId()
        );

        response.setOrderId(
                payment.getOrder()
                        .getId()
        );

        response.setAmount(
                payment.getAmount()
        );

        response.setPaymentMethod(
                payment.getPaymentMethod()
        );

        response.setStatus(
                payment.getStatus()
        );

        response.setCreatedAt(
                payment.getCreatedAt()
        );

        response.setCompletedAt(
                payment.getCompletedAt()
        );

        return response;
    }
}
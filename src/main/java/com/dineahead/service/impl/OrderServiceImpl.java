package com.dineahead.service.impl;

import com.dineahead.dto.OrderItemRequestDTO;
import com.dineahead.dto.OrderItemResponseDTO;
import com.dineahead.dto.OrderRequestDTO;
import com.dineahead.dto.OrderResponseDTO;

import com.dineahead.entity.MenuItem;
import com.dineahead.entity.Order;
import com.dineahead.entity.OrderItem;
import com.dineahead.entity.Reservation;
import com.dineahead.entity.User;

import com.dineahead.enums.NotificationType;
import com.dineahead.enums.OrderStatus;
import com.dineahead.enums.ReservationStatus;

import com.dineahead.exception.InvalidReservationStatusException;
import com.dineahead.exception.ReservationNotFoundException;

import com.dineahead.repository.MenuItemRepository;
import com.dineahead.repository.OrderRepository;
import com.dineahead.repository.ReservationRepository;
import com.dineahead.repository.UserRepository;

import com.dineahead.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class OrderServiceImpl
        implements OrderService {


    @Autowired
    private OrderRepository orderRepository;


    @Autowired
    private ReservationRepository reservationRepository;


    @Autowired
    private MenuItemRepository menuItemRepository;


    @Autowired
    private ReservationOrderWindowService
            reservationOrderWindowService;


    // =================================================
    // ADDED FOR RESERVATION COMPLETION
    // =================================================
    @Autowired
    private ReservationCompletionService
            reservationCompletionService;


    @Autowired
    private OrderStatusService
            orderStatusService;


    // =================================================
    // ADDED FOR ORDER OWNERSHIP SECURITY
    // =================================================
    @Autowired
    private UserRepository userRepository;


    // =================================================
    // CREATE ORDER
    // =================================================

    @Override
    public OrderResponseDTO createOrder(
            OrderRequestDTO request) {


        // 1. Find reservation

        Reservation reservation =
                reservationRepository.findById(
                                request.getReservationId()
                        )
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation with id "
                                                + request.getReservationId()
                                                + " not found"
                                )
                        );


        // =================================================
        // ADDED FOR ORDER OWNERSHIP SECURITY
        //
        // Customer can create an order only for
        // their own reservation.
        //
        // Admin is allowed to access everything.
        // =================================================
        validateReservationOwnershipForOrder(
                reservation
        );


        // 2. Reservation must be confirmed

        if (reservation.getStatus()
                != ReservationStatus.CONFIRMED) {

            throw new InvalidReservationStatusException(
                    "Food cannot be ordered for reservation with status "
                            + reservation.getStatus()
            );
        }


        // 3. Prevent duplicate orders for the same reservation

        if (reservation.getOrder() != null) {

            throw new IllegalStateException(
                    "An order already exists for this reservation."
            );
        }


        // 4. Ordering window must be open

        if (!reservationOrderWindowService
                .isOrderingOpen(reservation)) {

            if (java.time.LocalDateTime.now()
                    .isAfter(
                            reservation.getReservationTime()
                    )
                    || java.time.LocalDateTime.now()
                    .isEqual(
                            reservation.getReservationTime()
                    )) {

                throw new IllegalStateException(
                        "Food ordering is closed because the reservation time has passed."
                );
            }

            throw new IllegalStateException(
                    "Food ordering is not open yet."
            );
        }


        // 5. Create order

        Order order = new Order();

        order.setReservation(reservation);

        order.setStatus(
                OrderStatus.PLACED
        );


        List<OrderItem> orderItems =
                new ArrayList<>();


        BigDecimal totalAmount =
                BigDecimal.ZERO;


        // 6. Process each requested menu item

        for (OrderItemRequestDTO itemRequest :
                request.getItems()) {


            MenuItem menuItem =
                    menuItemRepository.findById(
                                    itemRequest.getMenuItemId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Menu item with id "
                                                    + itemRequest.getMenuItemId()
                                                    + " not found"
                                    )
                            );


            // Menu item must belong to reservation restaurant

            if (!menuItem.getCategory()
                    .getRestaurant()
                    .getId()
                    .equals(
                            reservation.getRestaurant().getId()
                    )) {

                throw new IllegalStateException(
                        "Menu item "
                                + menuItem.getName()
                                + " does not belong to the reservation restaurant."
                );
            }


            // Menu item must be available

            if (!Boolean.TRUE.equals(
                    menuItem.getAvailable()
            )) {

                throw new IllegalStateException(
                        "Menu item "
                                + menuItem.getName()
                                + " is not available."
                );
            }


            // Get price from database

            BigDecimal price =
                    menuItem.getPrice();


            // Calculate subtotal

            BigDecimal subtotal =
                    price.multiply(
                            BigDecimal.valueOf(
                                    itemRequest.getQuantity()
                            )
                    );


            // Create OrderItem

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setOrder(order);

            orderItem.setMenuItem(menuItem);

            orderItem.setQuantity(
                    itemRequest.getQuantity()
            );

            orderItem.setPrice(
                    price
            );

            orderItems.add(
                    orderItem
            );

            totalAmount =
                    totalAmount.add(
                            subtotal
                    );
        }


        // Attach items

        order.setOrderItems(
                orderItems
        );


        // Set total amount

        order.setTotalAmount(
                totalAmount
        );


        // Save order

        Order savedOrder =
                orderRepository.save(
                        order
                );


        // Convert response

        return mapToResponseDTO(
                savedOrder
        );
    }


    // =================================================
    // UPDATE ORDER STATUS
    // =================================================

    @Override
    public OrderResponseDTO updateOrderStatus(
            Long orderId,
            OrderStatus status) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Order with id "
                                                + orderId
                                                + " not found"
                                )
                        );


        // =================================================
        // ORDER OWNERSHIP SECURITY
        // =================================================

        validateRestaurantOwnerAccess(
                order
        );


        // =================================================
        // ORDER STATUS TRANSITION VALIDATION
        // =================================================

        if (!orderStatusService.isValidTransition(
                order.getStatus(),
                status
        )) {

            throw new IllegalStateException(
                    "Invalid order status transition from "
                            + order.getStatus()
                            + " to "
                            + status
            );
        }


        // =================================================
        // UPDATE ORDER STATUS
        // =================================================

        order.setStatus(
                status
        );


        // Save order

        Order savedOrder =
                orderRepository.save(
                        order
                );


        // =================================================
        // RESERVATION COMPLETION
        // =================================================

        if (savedOrder.getStatus()
                == OrderStatus.SERVED) {

            reservationCompletionService
                    .completeReservationIfEligible(
                            savedOrder
                                    .getReservation()
                    );
        }


        return mapToResponseDTO(
                savedOrder
        );
    }

    @Override
    public OrderResponseDTO cancelOrder(
            Long orderId
    ) {

        // =================================================
        // 1. FIND ORDER
        // =================================================

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Order with id "
                                                + orderId
                                                + " not found"
                                )
                        );


        // =================================================
        // 2. ADMIN CAN CANCEL ANY ELIGIBLE ORDER
        // =================================================

        if (isAdmin()) {

            return cancelOrderIfAllowed(
                    order
            );
        }


        // =================================================
        // 3. RESTAURANT OWNER
        //
        // Can cancel only orders belonging to
        // their own restaurant.
        //
        // Allowed statuses:
        // PLACED
        // ACCEPTED
        // =================================================

        if (isRestaurantOwner()) {

            User currentUser =
                    getCurrentAuthenticatedUser();


            if (order.getReservation()
                    .getRestaurant()
                    .getOwner() == null
                    ||
                    !order.getReservation()
                            .getRestaurant()
                            .getOwner()
                            .getId()
                            .equals(
                                    currentUser.getId()
                            )) {

                throw new AccessDeniedException(
                        "You do not have permission to cancel this order."
                );
            }


            return cancelOrderIfAllowed(
                    order
            );
        }


        // =================================================
        // 4. CUSTOMER
        //
        // Customer can cancel only:
        //
        // - their own order
        // - while status is PLACED
        //
        // Once ACCEPTED, customer cannot cancel.
        // =================================================

        User currentUser =
                getCurrentAuthenticatedUser();


        if (order.getReservation()
                .getUser() == null
                ||
                !order.getReservation()
                        .getUser()
                        .getId()
                        .equals(
                                currentUser.getId()
                        )) {

            throw new AccessDeniedException(
                    "You do not have permission to cancel this order."
            );
        }


        if (order.getStatus()
                != OrderStatus.PLACED) {

            throw new IllegalStateException(
                    "Customer cannot cancel an order after it has been accepted."
            );
        }


        return cancelOrderIfAllowed(
                order
        );
    }


    // =================================================
    // GET ALL ORDERS
    // =================================================

    @Override
    public List<OrderResponseDTO> getAllOrders() {


        // =================================================
        // ADDED FOR ORDER OWNERSHIP SECURITY
        //
        // ADMIN -> all orders
        // RESTAURANT_OWNER -> only own restaurant orders
        // CUSTOMER -> only own orders
        // =================================================

        if (isAdmin()) {

            return orderRepository.findAll()
                    .stream()
                    .map(this::mapToResponseDTO)
                    .toList();
        }


        User currentUser =
                getCurrentAuthenticatedUser();


        if (isRestaurantOwner()) {

            return orderRepository.findAll()
                    .stream()
                    .filter(order ->
                            order.getReservation()
                                    .getRestaurant()
                                    .getOwner() != null
                                    &&
                                    order.getReservation()
                                            .getRestaurant()
                                            .getOwner()
                                            .getId()
                                            .equals(
                                                    currentUser.getId()
                                            )
                    )
                    .map(this::mapToResponseDTO)
                    .toList();
        }


        // CUSTOMER

        return orderRepository.findAll()
                .stream()
                .filter(order ->
                        order.getReservation().getUser() != null
                                &&
                                order.getReservation()
                                        .getUser()
                                        .getId()
                                        .equals(
                                                currentUser.getId()
                                        )
                )
                .map(this::mapToResponseDTO)
                .toList();
    }


    // =================================================
    // GET ORDER BY ID
    // =================================================

    @Override
    public OrderResponseDTO getOrderById(
            Long id
    ) {

        Order order =
                orderRepository.findById(
                                id
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Order with id "
                                                + id
                                                + " not found"
                                )
                        );


        // =================================================
        // ADDED FOR ORDER OWNERSHIP SECURITY
        // =================================================

        validateOrderAccess(
                order
        );


        return mapToResponseDTO(
                order
        );
    }


    // =================================================
    // GET ORDERS BY RESERVATION ID
    // =================================================

    @Override
    public List<OrderResponseDTO> getOrdersByReservationId(
            Long reservationId
    ) {


        // =================================================
        // ADDED FOR ORDER OWNERSHIP SECURITY
        //
        // We first find the reservation so ownership
        // can be validated even if no order exists.
        // =================================================

        Reservation reservation =
                reservationRepository.findById(
                                reservationId
                        )
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation with id "
                                                + reservationId
                                                + " not found"
                                )
                        );


        validateReservationAccess(
                reservation
        );


        Optional<Order> orderOptional =
                orderRepository.findByReservationId(
                        reservationId
                );


        return orderOptional
                .stream()
                .map(
                        this::mapToResponseDTO
                )
                .toList();
    }


    // =================================================
    // GET KITCHEN ORDERS
    // =================================================

    @Override
    public List<OrderResponseDTO> getKitchenOrders() {


        List<OrderStatus> kitchenStatuses =
                List.of(
                        OrderStatus.PLACED,
                        OrderStatus.ACCEPTED,
                        OrderStatus.PREPARING,
                        OrderStatus.READY
                );


        List<Order> orders =
                orderRepository.findByStatusIn(
                        kitchenStatuses
                );


        // =================================================
        // ADDED FOR ORDER OWNERSHIP SECURITY
        //
        // ADMIN -> kitchen orders from all restaurants
        // RESTAURANT_OWNER -> kitchen orders only
        //                     from own restaurants
        // CUSTOMER -> no kitchen access
        // =================================================

        if (isAdmin()) {

            return orders.stream()
                    .map(this::mapToResponseDTO)
                    .toList();
        }


        if (!isRestaurantOwner()) {

            throw new AccessDeniedException(
                    "You do not have permission to access kitchen orders."
            );
        }


        User currentUser =
                getCurrentAuthenticatedUser();


        return orders.stream()
                .filter(order ->
                        order.getReservation()
                                .getRestaurant()
                                .getOwner() != null
                                &&
                                order.getReservation()
                                        .getRestaurant()
                                        .getOwner()
                                        .getId()
                                        .equals(
                                                currentUser.getId()
                                        )
                )
                .map(this::mapToResponseDTO)
                .toList();
    }


    // =================================================
    // ADDED FOR ORDER OWNERSHIP SECURITY
    //
    // GET CURRENT AUTHENTICATED USER
    // =================================================

    private User getCurrentAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        String email =
                authentication.getName();


        return userRepository
                .findByEmail(
                        email
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found."
                        )
                );
    }


    // =================================================
    // ADDED FOR ORDER OWNERSHIP SECURITY
    //
    // CHECK ADMIN ROLE
    // =================================================

    private boolean isAdmin() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(
                        authority ->
                                authority.getAuthority()
                                        .equals(
                                                "ROLE_ADMIN"
                                        )
                );
    }


    // =================================================
    // ADDED FOR ORDER OWNERSHIP SECURITY
    //
    // CHECK RESTAURANT OWNER ROLE
    // =================================================

    private boolean isRestaurantOwner() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(
                        authority ->
                                authority.getAuthority()
                                        .equals(
                                                "ROLE_RESTAURANT_OWNER"
                                        )
                );
    }


    // =================================================
    // ADDED FOR ORDER OWNERSHIP SECURITY
    //
    // CUSTOMER CAN CREATE ORDER ONLY FOR
    // THEIR OWN RESERVATION.
    //
    // ADMIN IS ALSO ALLOWED.
    // =================================================

    private void validateReservationOwnershipForOrder(
            Reservation reservation
    ) {

        if (isAdmin()) {

            return;
        }


        User currentUser =
                getCurrentAuthenticatedUser();


        if (reservation.getUser() == null
                || !reservation
                .getUser()
                .getId()
                .equals(
                        currentUser.getId()
                )) {

            throw new AccessDeniedException(
                    "You do not have permission to create an order for this reservation."
            );
        }
    }


    // =================================================
    // ADDED FOR ORDER OWNERSHIP SECURITY
    //
    // VALIDATE ACCESS TO AN ORDER
    // =================================================

    private void validateOrderAccess(
            Order order
    ) {

        // ADMIN can access all orders

        if (isAdmin()) {

            return;
        }


        User currentUser =
                getCurrentAuthenticatedUser();


        // RESTAURANT OWNER can access orders
        // belonging to their restaurant

        if (isRestaurantOwner()) {

            if (order.getReservation()
                    .getRestaurant()
                    .getOwner() != null
                    &&
                    order.getReservation()
                            .getRestaurant()
                            .getOwner()
                            .getId()
                            .equals(
                                    currentUser.getId()
                            )) {

                return;
            }


            throw new AccessDeniedException(
                    "You do not have permission to access this order."
            );
        }


        // CUSTOMER can access only own orders

        if (order.getReservation()
                .getUser() != null
                &&
                order.getReservation()
                        .getUser()
                        .getId()
                        .equals(
                                currentUser.getId()
                        )) {

            return;
        }


        throw new AccessDeniedException(
                "You do not have permission to access this order."
        );
    }


    // =================================================
    // ADDED FOR ORDER OWNERSHIP SECURITY
    //
    // VALIDATE ACCESS TO A RESERVATION
    // WHEN RETRIEVING ITS ORDER
    // =================================================

    private void validateReservationAccess(
            Reservation reservation
    ) {

        // ADMIN can access everything

        if (isAdmin()) {

            return;
        }


        User currentUser =
                getCurrentAuthenticatedUser();


        // RESTAURANT OWNER

        if (isRestaurantOwner()) {

            if (reservation.getRestaurant()
                    .getOwner() != null
                    &&
                    reservation
                            .getRestaurant()
                            .getOwner()
                            .getId()
                            .equals(
                                    currentUser.getId()
                            )) {

                return;
            }


            throw new AccessDeniedException(
                    "You do not have permission to access this reservation's order."
            );
        }


        // CUSTOMER

        if (reservation.getUser() != null
                &&
                reservation
                        .getUser()
                        .getId()
                        .equals(
                                currentUser.getId()
                        )) {

            return;
        }


        throw new AccessDeniedException(
                "You do not have permission to access this reservation's order."
        );
    }


    // =================================================
    // ADDED FOR ORDER OWNERSHIP SECURITY
    //
    // ONLY RESTAURANT OWNER OR ADMIN
    // CAN UPDATE ORDER STATUS
    // =================================================

    private void validateRestaurantOwnerAccess(
            Order order
    ) {

        // ADMIN can update every order

        if (isAdmin()) {

            return;
        }


        // Must be a RESTAURANT_OWNER

        if (!isRestaurantOwner()) {

            throw new AccessDeniedException(
                    "Only the restaurant owner can update order status."
            );
        }


        User currentUser =
                getCurrentAuthenticatedUser();


        if (order.getReservation()
                .getRestaurant()
                .getOwner() == null
                ||
                !order.getReservation()
                        .getRestaurant()
                        .getOwner()
                        .getId()
                        .equals(
                                currentUser.getId()
                        )) {

            throw new AccessDeniedException(
                    "You do not have permission to update this order."
            );
        }
    }


    // =================================================
    // MAP TO RESPONSE DTO
    // =================================================

    private OrderResponseDTO mapToResponseDTO(
            Order order
    ) {


        List<OrderItemResponseDTO> itemResponses =
                new ArrayList<>();


        BigDecimal totalAmount =
                BigDecimal.ZERO;


        for (OrderItem item :
                order.getOrderItems()) {


            BigDecimal subtotal =
                    item.getPrice().multiply(
                            BigDecimal.valueOf(
                                    item.getQuantity()
                            )
                    );


            OrderItemResponseDTO response =
                    new OrderItemResponseDTO();


            response.setMenuItemId(
                    item.getMenuItem()
                            .getId()
            );


            response.setMenuItemName(
                    item.getMenuItem()
                            .getName()
            );


            response.setQuantity(
                    item.getQuantity()
            );


            response.setPrice(
                    item.getPrice()
            );


            response.setSubtotal(
                    subtotal
            );


            itemResponses.add(
                    response
            );


            totalAmount =
                    totalAmount.add(
                            subtotal
                    );
        }


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
                totalAmount
        );


        response.setItems(
                itemResponses
        );


        return response;
    }

    private OrderResponseDTO cancelOrderIfAllowed(
            Order order
    ) {

        // =================================================
        // VALIDATE STATUS TRANSITION
        // =================================================

        if (!orderStatusService.isValidTransition(
                order.getStatus(),
                OrderStatus.CANCELLED
        )) {

            throw new IllegalStateException(
                    "Order with status "
                            + order.getStatus()
                            + " cannot be cancelled."
            );
        }


        // =================================================
        // CANCEL ORDER
        // =================================================

        order.setStatus(
                OrderStatus.CANCELLED
        );


        Order savedOrder =
                orderRepository.save(
                        order
                );


        return mapToResponseDTO(
                savedOrder
        );
    }

}
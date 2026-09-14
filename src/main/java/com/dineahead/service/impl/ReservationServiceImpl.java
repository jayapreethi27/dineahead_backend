package com.dineahead.service.impl;

import com.dineahead.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.dineahead.dto.*;
import com.dineahead.entity.*;
import com.dineahead.enums.ReservationStatus;
import com.dineahead.exception.*;
import com.dineahead.repository.PaymentRepository;
import com.dineahead.repository.ReservationRepository;
import com.dineahead.repository.RestaurantRepository;
import com.dineahead.repository.RestaurantTableRepository;
import com.dineahead.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.dineahead.constants.ReservationConstants.RESERVATION_DURATION_HOURS;

import com.dineahead.enums.NotificationType;
import com.dineahead.service.NotificationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    //Repository injection
    private final ReservationRepository reservationRepository;  //Save the reservation

    private final RestaurantRepository restaurantRepository;    //Verify that the restaurant exist

    private final RestaurantTableRepository restaurantTableRepository;  //Verify that the table exists

    //private static final int RESERVATION_DURATION_HOURS = 2;

    // ==========================================
// ADDED FOR OWNERSHIP SECURITY
// ==========================================
    private final UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;


    @Override
    public ReservationResponseDTO saveReservation(ReservationRequestDTO request) {

        if (!request.getReservationTime()
                .isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Reservation time must be in the future."
            );
        }

        Restaurant restaurant = findRestaurantById(request.getRestaurantId());

        RestaurantTable restaurantTable = findRestaurantTableById(request.getRestaurantTableId());

        validateTableOwnership(restaurant, restaurantTable);

        validatePartySize(request.getPartySize(), restaurantTable);

       validateReservationOverlap(
               request.getRestaurantTableId(),
               request.getReservationTime()
       );

        Reservation reservation = new Reservation();

        // ==========================================
// ADDED FOR OWNERSHIP SECURITY
// ==========================================
        User currentUser =
                getCurrentAuthenticatedUser();

        reservation.setUser(
                currentUser
        );

        reservation.setCustomerName(request.getCustomerName());
        reservation.setReservationTime(request.getReservationTime());
        reservation.setPartySize(request.getPartySize());
        reservation.setRestaurant(restaurant);
        reservation.setRestaurantTable(restaurantTable);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        Reservation savedReservation = reservationRepository.save(reservation);

        return mapToResponseDTO(savedReservation);
    }

    @Override
    public ReservationResponseDTO getReservationById(
            Long id
    ) {

        Reservation reservation =
                findReservationById(id);


        // ==========================================
        // ADDED FOR OWNERSHIP SECURITY
        // ==========================================
        validateReservationViewAccess(
                reservation
        );


        return mapToResponseDTO(
                reservation
        );
    }

    @Override
    public List<ReservationResponseDTO> getAllReservations() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isAdmin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority.getAuthority()
                                                .equals(
                                                        "ROLE_ADMIN"
                                                )
                        );


        if (!isAdmin) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to view all reservations."
            );
        }


        List<Reservation> reservations =
                reservationRepository.findAll();


        return reservations.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public ReservationResponseDTO updateReservation(Long id, ReservationRequestDTO request) {

        Reservation reservation = findReservationById(id);

        // ==========================================
// ADDED FOR OWNERSHIP SECURITY
// ==========================================

        validateReservationOwnership(
                reservation
        );

        Restaurant restaurant = findRestaurantById(request.getRestaurantId());

        RestaurantTable restaurantTable = findRestaurantTableById(request.getRestaurantTableId());

        validateTableOwnership(restaurant, restaurantTable);

        validatePartySize(request.getPartySize(), restaurantTable);

//        boolean reservationExists = reservationRepository
//                .existsByRestaurantTableIdAndReservationTimeAndIdNot(
//                        request.getRestaurantTableId(),
//                        request.getReservationTime(),
//                        id
//                );
//
//        if(reservationExists){
//            throw new DuplicateReservationException(
//                    "This table is already reserved for the selected time."
//            );
//        }
        /*List<Reservation> existingReservations =
                reservationRepository.findByRestaurantTable_Id(
                        request.getRestaurantTableId()
                );

        System.out.println("Existing reservations found: " +existingReservations.size());

        for (Reservation existingReservation : existingReservations){

            System.out.println("Existing reservation time: " +existingReservation.getReservationTime());
            System.out.println("New reservation time: " +request.getReservationTime());

            if(isTimeOverlapping(
                    existingReservation.getReservationTime(),
                    request.getReservationTime())){

                throw new DuplicateReservationException(
                        "This table is already reserved during the selected time window."
                );
            }
        } */

        validateReservationOverlap(
                request.getRestaurantTableId(),
                request.getReservationTime(),
                id
        );

        reservation.setCustomerName(request.getCustomerName());
        reservation.setReservationTime(request.getReservationTime());
        reservation.setPartySize(request.getPartySize());
        reservation.setRestaurant(restaurant);
        reservation.setRestaurantTable(restaurantTable);

        Reservation updatedReservation = reservationRepository.save(reservation);

        return mapToResponseDTO(updatedReservation);
    }

    @Override
    public void deleteReservation(
            Long id
    ) {

        Reservation reservation =
                findReservationById(id);


        // ==========================================
        // ADDED FOR OWNERSHIP SECURITY
        // ==========================================
        validateReservationOwnership(
                reservation
        );


        reservationRepository.delete(
                reservation
        );
    }
    private Reservation findReservationById(Long id){

        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ReservationNotFoundException(
                                "Reservation with id " +id+ " not found"
                        )
                );
    }

    private LocalDateTime getReservationEndTime(
            LocalDateTime startTime) {

        return startTime.plusHours(
                RESERVATION_DURATION_HOURS
        );
    }

    private boolean isTimeOverlapping(
            LocalDateTime existingStart,
            LocalDateTime newStart){

        LocalDateTime existingEnd = getReservationEndTime(existingStart);
        LocalDateTime newEnd = getReservationEndTime(newStart);

        return existingStart.isBefore(newEnd) && newStart.isBefore(existingEnd);
    }

    private void validateReservationOverlap(
            Long restaurantTableId,
            LocalDateTime reservationTime) {

        List<Reservation> existingReservations =
                reservationRepository.findByRestaurantTable_Id(
                        restaurantTableId
                );

        for (Reservation existingReservation :
                existingReservations) {

            // Cancelled reservations must NOT block the table
            if (existingReservation.getStatus()
                    == ReservationStatus.CANCELLED) {

                continue;
            }

            if (isTimeOverlapping(
                    existingReservation.getReservationTime(),
                    reservationTime)) {

                throw new DuplicateReservationException(
                        "This table is already reserved during the selected time window."
                );
            }
        }
    }

    private void validateReservationOverlap(
            Long restaurantTableId,
            LocalDateTime reservationTime,
            Long reservationId) {

        List<Reservation> existingReservations =
                reservationRepository.findByRestaurantTable_Id(
                        restaurantTableId
                );

        for (Reservation existingReservation :
                existingReservations) {

            // Ignore the reservation being updated
            if (existingReservation.getId()
                    .equals(reservationId)) {

                continue;
            }

            // Cancelled reservations must NOT block the table
            if (existingReservation.getStatus()
                    == ReservationStatus.CANCELLED) {

                continue;
            }

            if (isTimeOverlapping(
                    existingReservation.getReservationTime(),
                    reservationTime)) {

                throw new DuplicateReservationException(
                        "This table is already reserved during the selected time window."
                );
            }
        }
    }

    private Restaurant findRestaurantById(Long id){
        return restaurantRepository.findById(id)
                .orElseThrow(() ->
                        new RestaurantNotFoundException(
                                "Restaurant with id " +id+ " not found"
                        )
                );
    }

    private RestaurantTable findRestaurantTableById(Long id){
        return restaurantTableRepository.findById(id)
                .orElseThrow(() ->
                        new RestaurantTableNotFoundException(
                                "Restaurant table with id " +id+ " not found"
                        )
                );
    }

    private void validateTableOwnership(
            Restaurant restaurant,
            RestaurantTable restaurantTable) {

        if(!restaurantTable.getRestaurant().getId().equals(
                restaurant.getId())) {

            throw new RuntimeException(
                    "The selected table does not belong to the selected restaurant."
            );
        }
    }

    private void validatePartySize(
            Integer partySize, RestaurantTable restaurantTable){

        if(partySize > restaurantTable.getCapacity()) {

            throw new PartySizeExceededException(
                    "Party size exceeds the table capacity."
            );
        }
    }

    private ReservationResponseDTO mapToResponseDTO(Reservation reservation){

        ReservationResponseDTO response = new ReservationResponseDTO();

        response.setId(reservation.getId());
        response.setCustomerName(reservation.getCustomerName());
        response.setReservationTime(reservation.getReservationTime());
        response.setPartySize(reservation.getPartySize());
        response.setRestaurantId(reservation.getRestaurant().getId());
        response.setRestaurantTableId(reservation.getRestaurantTable().getId());
        response.setTableNumber(reservation.getRestaurantTable().getTableNumber());
        response.setStatus(reservation.getStatus());

        return response;
    }

    @Override
    public List<ReservationResponseDTO>
    getReservationsByRestaurantId(
            Long restaurantId
    ) {

        // ==========================================
        // ADDED: RESTAURANT OWNERSHIP SECURITY
        // ==========================================
        validateRestaurantOwnership(
                restaurantId
        );


        List<Reservation> reservations =
                reservationRepository
                        .findByRestaurantId(
                                restaurantId
                        );


        return reservations.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public List<ReservationResponseDTO>
    getTodaysReservationsByRestaurantId(
            Long restaurantId
    ) {

        // ==========================================
        // ADDED: RESTAURANT OWNERSHIP SECURITY
        // ==========================================
        validateRestaurantOwnership(
                restaurantId
        );


        LocalDateTime startOfDay =
                LocalDateTime.now()
                        .toLocalDate()
                        .atStartOfDay();


        LocalDateTime endOfDay =
                startOfDay.plusDays(
                        1
                );


        List<Reservation> reservations =
                reservationRepository
                        .findByRestaurantIdAndReservationTimeBetween(
                                restaurantId,
                                startOfDay,
                                endOfDay
                        );


        return reservations.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public List<ReservationResponseDTO>
    getReservationsByCustomerName(
            String customerName
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isAdmin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority.getAuthority()
                                                .equals(
                                                        "ROLE_ADMIN"
                                                )
                        );


        /*
         * ADMIN can search reservations
         * using any customer name.
         */
        if (isAdmin) {

            List<Reservation> reservations =
                    reservationRepository
                            .findByCustomerName(
                                    customerName
                            );


            return reservations.stream()
                    .map(this::mapToResponseDTO)
                    .toList();
        }


        /*
         * CUSTOMER cannot choose another
         * customer name from the URL.
         *
         * Always use the authenticated user.
         */
        User currentUser =
                getCurrentAuthenticatedUser();


        List<Reservation> reservations =
                reservationRepository
                        .findByUserId(
                                currentUser.getId()
                        );


        return reservations.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public void cancelReservation(Long id) {
        Reservation reservation = findReservationById(id);

        // ==========================================
        // ADDED FOR OWNERSHIP SECURITY
        // ==========================================
        validateReservationOwnership(
                reservation
        );

        if(reservation.getStatus()!=ReservationStatus.CONFIRMED){
            throw new InvalidReservationStatusException(
                    "Only confirmed reservations can be cancelled."
            );
        }
        reservation.setStatus(ReservationStatus.CANCELLED);

        reservationRepository.save(
                reservation
        );
    }

    @Override
    public void completeReservation(Long id) {

        Reservation reservation =
                findReservationById(id);


        // ==========================================
        // ADMIN or restaurant owner authorization
        // ==========================================
        validateReservationCompletionAccess(
                reservation
        );


        if (reservation.getStatus()
                != ReservationStatus.CONFIRMED) {

            throw new InvalidReservationStatusException(
                    "Only confirmed reservations can be completed."
            );
        }


        reservation.setStatus(
                ReservationStatus.COMPLETED
        );


        reservationRepository.save(
                reservation
        );
    }

    private void validateReservationCompletionAccess(
            Reservation reservation
    ) {

        // ==========================================
        // ADMIN can complete any reservation
        // ==========================================

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isAdmin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equals(
                                                        "ROLE_ADMIN"
                                                )
                        );


        if (isAdmin) {

            return;
        }


        // ==========================================
        // Only RESTAURANT_OWNER can complete
        // reservations manually
        // ==========================================

        boolean isRestaurantOwner =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equals(
                                                        "ROLE_RESTAURANT_OWNER"
                                                )
                        );


        if (!isRestaurantOwner) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "Only the restaurant owner or admin can complete a reservation."
            );
        }


        User currentUser =
                getCurrentAuthenticatedUser();


        // ==========================================
        // Owner must own the reservation's restaurant
        // ==========================================

        if (reservation
                .getRestaurant()
                .getOwner() == null

                ||

                !reservation
                        .getRestaurant()
                        .getOwner()
                        .getId()
                        .equals(
                                currentUser.getId()
                        )) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to complete this reservation."
            );
        }
    }

    @Override
    public CustomerHistoryResponseDTO getCustomerHistory(
            String customerName
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isAdmin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority.getAuthority()
                                                .equals(
                                                        "ROLE_ADMIN"
                                                )
                        );


        List<Reservation> reservations;

        String responseCustomerName;


        /*
         * ADMIN can view history
         * for any customer.
         */
        if (isAdmin) {

            reservations =
                    reservationRepository
                            .findByCustomerName(
                                    customerName
                            );

            responseCustomerName =
                    customerName;

        } else {

            /*
             * CUSTOMER cannot choose
             * another customer's history.
             */
            User currentUser =
                    getCurrentAuthenticatedUser();


            reservations =
                    reservationRepository
                            .findByUserId(
                                    currentUser.getId()
                            );


            responseCustomerName =
                    currentUser.getName();
        }


        List<CustomerHistoryReservationDTO> history =
                new ArrayList<>();


        for (Reservation reservation :
                reservations) {

            CustomerHistoryReservationDTO historyDTO =
                    new CustomerHistoryReservationDTO();


            historyDTO.setReservationId(
                    reservation.getId()
            );

            historyDTO.setCustomerName(
                    reservation.getCustomerName()
            );

            historyDTO.setReservationTime(
                    reservation.getReservationTime()
            );

            historyDTO.setPartySize(
                    reservation.getPartySize()
            );

            historyDTO.setRestaurantId(
                    reservation.getRestaurant()
                            .getId()
            );

            historyDTO.setRestaurantTableId(
                    reservation.getRestaurantTable()
                            .getId()
            );

            historyDTO.setReservationStatus(
                    reservation.getStatus()
            );


            // ==========================================
            // ORDER
            // ==========================================

            Order order =
                    reservation.getOrder();


            if (order != null) {

                OrderResponseDTO orderResponse =
                        mapOrderToResponseDTO(
                                order
                        );


                historyDTO.setOrder(
                        orderResponse
                );


                // ==========================================
                // PAYMENT
                // ==========================================

                Payment payment =
                        paymentRepository
                                .findByOrderId(
                                        order.getId()
                                )
                                .orElse(null);


                if (payment != null) {

                    PaymentResponseDTO paymentResponse =
                            mapPaymentToResponseDTO(
                                    payment
                            );


                    historyDTO.setPayment(
                            paymentResponse
                    );
                }
            }


            history.add(
                    historyDTO
            );
        }


        CustomerHistoryResponseDTO response =
                new CustomerHistoryResponseDTO();


        response.setCustomerName(
                responseCustomerName
        );


        response.setReservations(
                history
        );


        return response;
    }

    private OrderResponseDTO mapOrderToResponseDTO(
            Order order) {

        OrderResponseDTO response =
                new OrderResponseDTO();

        response.setId(
                order.getId()
        );

        // Reservation
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

        // Restaurant
        response.setRestaurantId(
                order.getReservation()
                        .getRestaurant()
                        .getId()
        );

        // Table
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

        // Order
        response.setStatus(
                order.getStatus()
        );

        response.setTotalAmount(
                order.getTotalAmount()
        );

        // Order items
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

    private PaymentResponseDTO mapPaymentToResponseDTO(
            Payment payment) {

        PaymentResponseDTO response =
                new PaymentResponseDTO();

        response.setId(
                payment.getId()
        );

        response.setOrderId(
                payment.getOrder().getId()
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

    // ==========================================
// ADDED FOR OWNERSHIP SECURITY
// ==========================================
    private User getCurrentAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        String email =
                authentication.getName();


        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found."
                        )
                );
    }

    private void validateReservationViewAccess(
            Reservation reservation
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        // ADMIN can view any reservation
        boolean isAdmin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority.getAuthority()
                                                .equals(
                                                        "ROLE_ADMIN"
                                                )
                        );

        if (isAdmin) {
            return;
        }


        // RESTAURANT OWNER can view
        // reservations belonging to their restaurant
        boolean isRestaurantOwner =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority.getAuthority()
                                                .equals(
                                                        "ROLE_RESTAURANT_OWNER"
                                                )
                        );

        if (isRestaurantOwner) {

            validateRestaurantOwnership(
                    reservation
                            .getRestaurant()
                            .getId()
            );

            return;
        }


        // CUSTOMER can view only their own reservation
        User currentUser =
                getCurrentAuthenticatedUser();

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


        throw new org.springframework.security.access.AccessDeniedException(
                "You do not have permission to access this reservation."
        );
    }

    // ==========================================
// ADDED FOR OWNERSHIP SECURITY
// ==========================================
    private void validateReservationOwnership(
            Reservation reservation
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isAdmin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority.getAuthority()
                                                .equals(
                                                        "ROLE_ADMIN"
                                                )
                        );


        // ADMIN can access all reservations
        if (isAdmin) {

            return;
        }


        User currentUser =
                getCurrentAuthenticatedUser();


        // Customer must own the reservation
        if (!reservation
                .getUser()
                .getId()
                .equals(
                        currentUser.getId()
                )) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to access this reservation."
            );
        }
    }

    // ==========================================
// ADDED: RESTAURANT OWNERSHIP SECURITY
// ==========================================
    private void validateRestaurantOwnership(
            Long restaurantId
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isAdmin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority.getAuthority()
                                                .equals(
                                                        "ROLE_ADMIN"
                                                )
                        );


        // ADMIN can access all restaurants
        if (isAdmin) {

            return;
        }


        boolean isRestaurantOwner =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority.getAuthority()
                                                .equals(
                                                        "ROLE_RESTAURANT_OWNER"
                                                )
                        );


        // Other roles cannot access restaurant reservations
        if (!isRestaurantOwner) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to access restaurant reservations."
            );
        }


        User currentUser =
                getCurrentAuthenticatedUser();


        Restaurant restaurant =
                findRestaurantById(
                        restaurantId
                );


        // Owner must own this restaurant
        if (restaurant.getOwner() == null
                ||
                !restaurant
                        .getOwner()
                        .getId()
                        .equals(
                                currentUser.getId()
                        )) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to access this restaurant."
            );
        }
    }
}

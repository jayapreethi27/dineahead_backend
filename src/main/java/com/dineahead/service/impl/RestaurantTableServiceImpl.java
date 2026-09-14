package com.dineahead.service.impl;

import com.dineahead.dto.RestaurantTableRequestDTO;
import com.dineahead.dto.RestaurantTableResponseDTO;
import com.dineahead.entity.Reservation;
import com.dineahead.entity.Restaurant;
import com.dineahead.entity.RestaurantTable;
import com.dineahead.entity.User;
import com.dineahead.enums.ReservationStatus;
import com.dineahead.exception.DuplicateTableException;
import com.dineahead.exception.RestaurantNotFoundException;
import com.dineahead.exception.RestaurantTableNotFoundException;
import com.dineahead.repository.ReservationRepository;
import com.dineahead.repository.RestaurantRepository;
import com.dineahead.repository.RestaurantTableRepository;
import com.dineahead.repository.UserRepository;
import com.dineahead.service.RestaurantTableService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.dineahead.constants.ReservationConstants.RESERVATION_DURATION_HOURS;


@Service
@RequiredArgsConstructor
public class RestaurantTableServiceImpl
        implements RestaurantTableService {


    private final RestaurantTableRepository
            restaurantTableRepository;

    private final ReservationRepository
            reservationRepository;

    private final RestaurantRepository
            restaurantRepository;


    // ==========================================
    // ADDED FOR RESTAURANT TABLE OWNERSHIP
    // ==========================================
    private final UserRepository
            userRepository;


    @Override
    public RestaurantTableResponseDTO saveRestaurantTable(
            RestaurantTableRequestDTO request
    ) {

        Restaurant restaurant =
                restaurantRepository
                        .findById(
                                request.getRestaurantId()
                        )
                        .orElseThrow(() ->
                                new RestaurantNotFoundException(
                                        "Restaurant with id "
                                                + request.getRestaurantId()
                                                + " not found"
                                )
                        );


        // ==========================================
        // ADDED FOR RESTAURANT TABLE OWNERSHIP
        // ==========================================
        validateRestaurantOwnership(
                restaurant
        );


        if (restaurantTableRepository
                .existsByRestaurantIdAndTableNumber(
                        request.getRestaurantId(),
                        request.getTableNumber()
                )) {

            throw new DuplicateTableException(
                    "Table number "
                            + request.getTableNumber()
                            + " already exists for this restaurant"
            );
        }


        RestaurantTable restaurantTable =
                new RestaurantTable();

        restaurantTable.setTableNumber(
                request.getTableNumber()
        );

        restaurantTable.setCapacity(
                request.getCapacity()
        );

        restaurantTable.setRestaurant(
                restaurant
        );


        RestaurantTable savedRestaurantTable =
                restaurantTableRepository.save(
                        restaurantTable
                );


        return mapToResponseDTO(
                savedRestaurantTable
        );
    }


    @Override
    public RestaurantTableResponseDTO
    getRestaurantTableById(
            Long id
    ) {

        RestaurantTable restaurantTable =
                findRestaurantTableById(
                        id
                );


        return mapToResponseDTO(
                restaurantTable
        );
    }


    @Override
    public List<RestaurantTableResponseDTO>
    getTablesByRestaurantId(
            Long restaurantId
    ) {

        List<RestaurantTable> tables =
                restaurantTableRepository
                        .findByRestaurantId(
                                restaurantId
                        );


        return tables.stream()
                .map(
                        this::mapToResponseDTO
                )
                .toList();
    }


    @Override
    public RestaurantTableResponseDTO
    updatedRestaurantTable(
            Long id,
            RestaurantTableRequestDTO request
    ) {

        RestaurantTable restaurantTable =
                findRestaurantTableById(
                        id
                );


        // ==========================================
        // ADDED FOR RESTAURANT TABLE OWNERSHIP
        //
        // Owner must own the restaurant where the
        // existing table currently belongs.
        // ==========================================
        validateRestaurantOwnership(
                restaurantTable.getRestaurant()
        );


        Restaurant restaurant =
                restaurantRepository
                        .findById(
                                request.getRestaurantId()
                        )
                        .orElseThrow(() ->
                                new RestaurantNotFoundException(
                                        "Restaurant with id "
                                                + request.getRestaurantId()
                                                + " not found"
                                )
                        );


        // ==========================================
        // ADDED FOR RESTAURANT TABLE OWNERSHIP
        //
        // Prevent an owner from moving a table to
        // another owner's restaurant.
        // ==========================================
        validateRestaurantOwnership(
                restaurant
        );


        // ==========================================
        // ADDED FOR DUPLICATE TABLE VALIDATION
        // ==========================================
        boolean duplicateTableExists =
                restaurantTableRepository
                        .existsByRestaurantIdAndTableNumber(
                                request.getRestaurantId(),
                                request.getTableNumber()
                        );


        if (duplicateTableExists
                && !restaurantTable
                .getRestaurant()
                .getId()
                .equals(request.getRestaurantId())

                || duplicateTableExists
                && restaurantTable
                .getRestaurant()
                .getId()
                .equals(request.getRestaurantId())
                && !restaurantTable
                .getTableNumber()
                .equals(request.getTableNumber())
        ) {

            throw new DuplicateTableException(
                    "Table number "
                            + request.getTableNumber()
                            + " already exists for this restaurant"
            );
        }


        restaurantTable.setTableNumber(
                request.getTableNumber()
        );

        restaurantTable.setCapacity(
                request.getCapacity()
        );

        restaurantTable.setRestaurant(
                restaurant
        );


        RestaurantTable updatedRestaurantTable =
                restaurantTableRepository.save(
                        restaurantTable
                );


        return mapToResponseDTO(
                updatedRestaurantTable
        );
    }


    @Override
    public void deleteRestaurantTable(
            Long id
    ) {

        RestaurantTable restaurantTable =
                restaurantTableRepository.findById(id)
                        .orElseThrow(() ->
                                new RestaurantTableNotFoundException(
                                        "Restaurant table with id "
                                                + id
                                                + " not found."
                                )
                        );


        // ==========================================
        // ADDED FOR RESTAURANT TABLE OWNERSHIP
        // ==========================================
        validateRestaurantOwnership(
                restaurantTable.getRestaurant()
        );


        restaurantTableRepository.delete(
                restaurantTable
        );
    }


    @Override
    public List<RestaurantTableResponseDTO>
    getAvailableTables(
            Long restaurantId,
            LocalDateTime reservationTime,
            Integer partySize
    ) {

        List<RestaurantTable> tables =
                restaurantTableRepository
                        .findByRestaurantIdAndCapacityGreaterThanEqual(
                                restaurantId,
                                partySize
                        );


        List<RestaurantTable> availableTables =
                new ArrayList<>();


        for (RestaurantTable table : tables) {

            List<Reservation> reservations =
                    reservationRepository
                            .findByRestaurantTable_Id(
                                    table.getId()
                            );


            boolean tableReserved =
                    false;


            for (Reservation reservation :
                    reservations) {

                if (reservation.getStatus()
                        == ReservationStatus.CANCELLED) {

                    continue;
                }


                LocalDateTime existingStart =
                        reservation
                                .getReservationTime();


                LocalDateTime existingEnd =
                        existingStart.plusHours(
                                RESERVATION_DURATION_HOURS
                        );


                LocalDateTime newEnd =
                        reservationTime.plusHours(
                                RESERVATION_DURATION_HOURS
                        );


                boolean overlapping =
                        existingStart.isBefore(
                                newEnd
                        )
                                &&
                                reservationTime.isBefore(
                                        existingEnd
                                );


                if (overlapping) {

                    tableReserved = true;

                    break;
                }
            }


            if (!tableReserved) {

                availableTables.add(
                        table
                );
            }
        }


        return availableTables
                .stream()
                .map(
                        this::mapToResponseDTO
                )
                .toList();
    }


    // ==========================================
    // ADDED FOR RESTAURANT TABLE OWNERSHIP
    // ==========================================
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


    // ==========================================
    // ADDED FOR RESTAURANT TABLE OWNERSHIP
    // ==========================================
    private void validateRestaurantOwnership(
            Restaurant restaurant
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
                                        authority
                                                .getAuthority()
                                                .equals(
                                                        "ROLE_ADMIN"
                                                )
                        );


        // ADMIN can manage all restaurants
        if (isAdmin) {

            return;
        }


        User currentUser =
                getCurrentAuthenticatedUser();


        if (restaurant.getOwner() == null ||
                !restaurant.getOwner()
                        .getId()
                        .equals(
                                currentUser.getId()
                        )) {

            throw new AccessDeniedException(
                    "You do not have permission to manage this restaurant table."
            );
        }

    }


    private RestaurantTable
    findRestaurantTableById(
            Long id
    ) {

        return restaurantTableRepository
                .findById(
                        id
                )
                .orElseThrow(() ->
                        new RestaurantTableNotFoundException(
                                "Restaurant table with id "
                                        + id
                                        + " not found"
                        )
                );
    }


    private RestaurantTableResponseDTO
    mapToResponseDTO(
            RestaurantTable restaurantTable
    ) {

        RestaurantTableResponseDTO response =
                new RestaurantTableResponseDTO();

        response.setId(
                restaurantTable.getId()
        );

        response.setTableNumber(
                restaurantTable.getTableNumber()
        );

        response.setCapacity(
                restaurantTable.getCapacity()
        );

        response.setRestaurantId(
                restaurantTable
                        .getRestaurant()
                        .getId()
        );


        return response;
    }
}
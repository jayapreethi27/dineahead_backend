package com.dineahead.service.impl;

import com.dineahead.dto.NotificationResponseDTO;
import com.dineahead.entity.Notification;
import com.dineahead.entity.Reservation;
import com.dineahead.entity.User;
import com.dineahead.enums.NotificationType;
import com.dineahead.enums.ReservationStatus;
import com.dineahead.exception.ReservationNotFoundException;
import com.dineahead.repository.NotificationRepository;
import com.dineahead.repository.ReservationRepository;
import com.dineahead.repository.UserRepository;
import com.dineahead.service.NotificationService;
import com.dineahead.service.ReservationOrderWindowService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class NotificationServiceImpl
        implements NotificationService {


    @Autowired
    private NotificationRepository notificationRepository;


    @Autowired
    private ReservationRepository reservationRepository;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private ReservationOrderWindowService
            reservationOrderWindowService;


    // =================================================
    // CREATE FOOD ORDERING NOTIFICATION
    // =================================================

    @Override
    public NotificationResponseDTO
    createFoodOrderingNotification(
            Long reservationId
    ) {

        return createNotification(
                reservationId,
                NotificationType.FOOD_ORDERING_OPEN,
                "Your food ordering window is now open. "
                        + "Please choose your dishes."
        );
    }


    // =================================================
    // GENERIC NOTIFICATION CREATION
    // =================================================

    @Override
    public NotificationResponseDTO createNotification(
            Long reservationId,
            NotificationType type,
            String message
    ) {

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


        Notification notification =
                new Notification();


        notification.setReservation(
                reservation
        );


        notification.setType(
                type
        );


        notification.setMessage(
                message
        );


        notification.setRead(
                false
        );


        notification.setCreatedAt(
                LocalDateTime.now()
        );


        Notification savedNotification =
                notificationRepository.save(
                        notification
                );


        return mapToResponseDTO(
                savedNotification
        );
    }


    // =================================================
    // GET NOTIFICATIONS BY RESERVATION ID
    // =================================================

    @Override
    public List<NotificationResponseDTO>
    getNotificationsByReservationId(
            Long reservationId
    ) {

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


        return notificationRepository
                .findByReservationIdOrderByCreatedAtDesc(
                        reservationId
                )
                .stream()
                .map(
                        this::mapToResponseDTO
                )
                .toList();
    }


    // =================================================
    // GET MY NOTIFICATIONS
    // =================================================

    @Override
    public List<NotificationResponseDTO>
    getMyNotifications() {

        User currentUser =
                getCurrentAuthenticatedUser();


        return notificationRepository
                .findByReservationUserIdOrderByCreatedAtDesc(
                        currentUser.getId()
                )
                .stream()
                .map(
                        this::mapToResponseDTO
                )
                .toList();
    }


    // =================================================
    // GET MY UNREAD NOTIFICATIONS
    // =================================================

    @Override
    public List<NotificationResponseDTO>
    getMyUnreadNotifications() {

        User currentUser =
                getCurrentAuthenticatedUser();


        return notificationRepository
                .findByReservationUserIdAndReadFalseOrderByCreatedAtDesc(
                        currentUser.getId()
                )
                .stream()
                .map(
                        this::mapToResponseDTO
                )
                .toList();
    }


    // =================================================
    // MARK NOTIFICATION AS READ
    // =================================================

    @Override
    public NotificationResponseDTO
    markNotificationAsRead(
            Long notificationId
    ) {

        Notification notification =
                notificationRepository.findById(
                                notificationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification with id "
                                                + notificationId
                                                + " not found"
                                )
                        );


        validateNotificationAccess(
                notification
        );


        notification.setRead(
                true
        );


        Notification savedNotification =
                notificationRepository.save(
                        notification
                );


        return mapToResponseDTO(
                savedNotification
        );
    }


    // =================================================
    // MARK ALL MY NOTIFICATIONS AS READ
    // =================================================

    @Override
    public void markAllNotificationsAsRead() {

        User currentUser =
                getCurrentAuthenticatedUser();


        List<Notification> notifications =
                notificationRepository
                        .findByReservationUserIdAndReadFalseOrderByCreatedAtDesc(
                                currentUser.getId()
                        );


        for (Notification notification :
                notifications) {

            notification.setRead(
                    true
            );
        }


        notificationRepository.saveAll(
                notifications
        );
    }


    // =================================================
    // FOOD ORDERING NOTIFICATION SCHEDULER
    // =================================================

    @Override
    @Scheduled(
            fixedRate = 60000
    )
    public void processFoodOrderingNotifications() {


        List<Reservation> reservations =
                reservationRepository.findAll();


        for (Reservation reservation :
                reservations) {


            // Reservation must be confirmed

            if (reservation.getStatus()
                    != ReservationStatus.CONFIRMED) {

                continue;
            }


            // Reservation must belong to a real user

            if (reservation.getUser()
                    == null) {

                continue;
            }


            // Ordering window must be open

            if (!reservationOrderWindowService
                    .isOrderingOpen(
                            reservation
                    )) {

                continue;
            }


            // Prevent duplicate notification

            boolean alreadyExists =
                    notificationRepository
                            .existsByReservationIdAndType(
                                    reservation.getId(),
                                    NotificationType
                                            .FOOD_ORDERING_OPEN
                            );


            if (alreadyExists) {

                continue;
            }


            createFoodOrderingNotification(
                    reservation.getId()
            );
        }
    }


    // =================================================
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
    // CHECK ADMIN
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
    // VALIDATE RESERVATION ACCESS
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


        // CUSTOMER can access own reservation

        if (reservation.getUser() != null
                &&
                reservation.getUser()
                        .getId()
                        .equals(
                                currentUser.getId()
                        )) {

            return;
        }


        throw new AccessDeniedException(
                "You do not have permission "
                        + "to access these notifications."
        );
    }


    // =================================================
    // VALIDATE NOTIFICATION ACCESS
    // =================================================

    private void validateNotificationAccess(
            Notification notification
    ) {

        validateReservationAccess(
                notification.getReservation()
        );
    }


    // =================================================
    // MAP TO RESPONSE DTO
    // =================================================

    private NotificationResponseDTO
    mapToResponseDTO(
            Notification notification
    ) {

        NotificationResponseDTO response =
                new NotificationResponseDTO();


        response.setId(
                notification.getId()
        );


        response.setReservationId(
                notification.getReservation()
                        .getId()
        );


        response.setType(
                notification.getType()
        );


        response.setMessage(
                notification.getMessage()
        );


        response.setRead(
                notification.getRead()
        );


        response.setCreatedAt(
                notification.getCreatedAt()
        );


        return response;
    }
}
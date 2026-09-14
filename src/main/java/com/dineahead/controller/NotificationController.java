package com.dineahead.controller;

import com.dineahead.dto.NotificationResponseDTO;
import com.dineahead.service.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/notifications")
public class NotificationController {


    @Autowired
    private NotificationService notificationService;


    // =================================================
    // CREATE FOOD ORDERING NOTIFICATION
    // =================================================

    @PostMapping(
            "/reservation/{reservationId}/food-ordering"
    )
    public ResponseEntity<NotificationResponseDTO>
    createFoodOrderingNotification(
            @PathVariable Long reservationId
    ) {

        NotificationResponseDTO response =
                notificationService
                        .createFoodOrderingNotification(
                                reservationId
                        );


        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        response
                );
    }


    // =================================================
    // GET NOTIFICATIONS BY RESERVATION
    // =================================================

    @GetMapping(
            "/reservation/{reservationId}"
    )
    public ResponseEntity<List<NotificationResponseDTO>>
    getNotificationsByReservationId(
            @PathVariable Long reservationId
    ) {

        List<NotificationResponseDTO> response =
                notificationService
                        .getNotificationsByReservationId(
                                reservationId
                        );


        return ResponseEntity.ok(
                response
        );
    }


    // =================================================
    // GET MY NOTIFICATIONS
    // =================================================

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>>
    getMyNotifications() {

        return ResponseEntity.ok(
                notificationService
                        .getMyNotifications()
        );
    }


    // =================================================
    // GET MY UNREAD NOTIFICATIONS
    // =================================================

    @GetMapping(
            "/unread"
    )
    public ResponseEntity<List<NotificationResponseDTO>>
    getMyUnreadNotifications() {

        return ResponseEntity.ok(
                notificationService
                        .getMyUnreadNotifications()
        );
    }


    // =================================================
    // MARK ONE NOTIFICATION AS READ
    // =================================================

    @PutMapping(
            "/{notificationId}/read"
    )
    public ResponseEntity<NotificationResponseDTO>
    markNotificationAsRead(
            @PathVariable Long notificationId
    ) {

        return ResponseEntity.ok(
                notificationService
                        .markNotificationAsRead(
                                notificationId
                        )
        );
    }


    // =================================================
    // MARK ALL MY NOTIFICATIONS AS READ
    // =================================================

    @PutMapping(
            "/read-all"
    )
    public ResponseEntity<Void>
    markAllNotificationsAsRead() {

        notificationService
                .markAllNotificationsAsRead();


        return ResponseEntity
                .noContent()
                .build();
    }
}
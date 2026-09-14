package com.dineahead.service;

import com.dineahead.dto.NotificationResponseDTO;
import com.dineahead.enums.NotificationType;

import java.util.List;

public interface NotificationService {


    // =================================================
    // EXISTING FOOD ORDERING NOTIFICATION
    // =================================================

    NotificationResponseDTO createFoodOrderingNotification(
            Long reservationId
    );


    // =================================================
    // GENERIC NOTIFICATION CREATION
    // =================================================

    NotificationResponseDTO createNotification(
            Long reservationId,
            NotificationType type,
            String message
    );


    // =================================================
    // GET NOTIFICATIONS BY RESERVATION
    // =================================================

    List<NotificationResponseDTO>
    getNotificationsByReservationId(
            Long reservationId
    );


    // =================================================
    // GET CURRENT USER NOTIFICATIONS
    // =================================================

    List<NotificationResponseDTO>
    getMyNotifications();


    // =================================================
    // GET CURRENT USER UNREAD NOTIFICATIONS
    // =================================================

    List<NotificationResponseDTO>
    getMyUnreadNotifications();


    // =================================================
    // MARK ONE NOTIFICATION AS READ
    // =================================================

    NotificationResponseDTO
    markNotificationAsRead(
            Long notificationId
    );


    // =================================================
    // MARK ALL CURRENT USER NOTIFICATIONS AS READ
    // =================================================

    void markAllNotificationsAsRead();


    // =================================================
    // SCHEDULER
    // =================================================

    void processFoodOrderingNotifications();
}
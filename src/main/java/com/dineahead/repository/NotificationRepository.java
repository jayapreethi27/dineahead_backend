package com.dineahead.repository;

import com.dineahead.entity.Notification;
import com.dineahead.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByReservationId(
            Long reservationId
    );

    boolean existsByReservationIdAndType(
            Long reservationId,
            NotificationType type
    );

    // ============================================
    // GET NOTIFICATIONS FOR A USER
    // ============================================

    List<Notification>
    findByReservationUserIdOrderByCreatedAtDesc(
            Long userId
    );


    // ============================================
    // GET UNREAD NOTIFICATIONS FOR A USER
    // ============================================

    List<Notification>
    findByReservationUserIdAndReadFalseOrderByCreatedAtDesc(
            Long userId
    );


    // ============================================
    // GET ALL NOTIFICATIONS FOR A RESERVATION
    // ============================================

    List<Notification>
    findByReservationIdOrderByCreatedAtDesc(
            Long reservationId
    );
}
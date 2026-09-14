package com.dineahead.dto;

import com.dineahead.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {

    private Long id;
    private Long reservationId;
    private NotificationType type;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;
}
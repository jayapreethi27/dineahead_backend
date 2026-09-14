package com.dineahead.service;

import com.dineahead.enums.OrderStatus;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusService {

    public boolean isValidTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus) {

        if (currentStatus == null || newStatus == null) {
            return false;
        }

        return switch (currentStatus) {

            case PLACED ->
                    newStatus == OrderStatus.ACCEPTED
                            || newStatus == OrderStatus.CANCELLED;

            case ACCEPTED ->
                    newStatus == OrderStatus.PREPARING
                            || newStatus == OrderStatus.CANCELLED;

            case PREPARING ->
                    newStatus == OrderStatus.READY;

            case READY ->
                    newStatus == OrderStatus.SERVED;

            case SERVED, CANCELLED ->
                    false;
        };
    }
}
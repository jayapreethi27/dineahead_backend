package com.dineahead.exception;

public class InvalidReservationStatusException extends RuntimeException{

    public InvalidReservationStatusException(String message){
        super(message);
    }
}

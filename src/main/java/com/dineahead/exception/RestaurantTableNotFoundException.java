package com.dineahead.exception;

public class RestaurantTableNotFoundException extends RuntimeException{

    public RestaurantTableNotFoundException(String message){
        super (message);
    }
}

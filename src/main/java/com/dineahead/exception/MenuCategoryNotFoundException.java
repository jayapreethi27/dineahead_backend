package com.dineahead.exception;

public class MenuCategoryNotFoundException extends RuntimeException {
    public MenuCategoryNotFoundException(String message) {
        super(message);
    }
}

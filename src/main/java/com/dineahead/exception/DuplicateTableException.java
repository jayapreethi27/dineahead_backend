package com.dineahead.exception;

public class DuplicateTableException extends RuntimeException{

    public DuplicateTableException(String message){
        super(message);
    }
}

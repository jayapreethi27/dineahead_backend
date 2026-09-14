package com.dineahead.exception;

public class PartySizeExceededException extends RuntimeException{

    public PartySizeExceededException( String message){
        super(message);
    }
}

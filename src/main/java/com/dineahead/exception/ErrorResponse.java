package com.dineahead.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorResponse {

    private boolean success;
    private String message;
    private Map<String, String> errors;
}

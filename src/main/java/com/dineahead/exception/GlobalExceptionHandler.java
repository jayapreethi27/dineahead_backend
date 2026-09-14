package com.dineahead.exception;

import com.dineahead.exception.InvalidReservationStatusException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(
            MethodArgumentNotValidException ex)
    {
        System.out.println("Global Exception Handler Executed");
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(
                    error.getField(),
                    error.getDefaultMessage());
        });

        return new ErrorResponse(
                false,
                "validation Failed",
                errors
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(
            UserNotFoundException ex) {

        return new ErrorResponse(
                false,
                ex.getMessage(),
                new HashMap<>()
        );
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleRestaurantNotFoundException(
            RestaurantNotFoundException ex){

        return new ErrorResponse(
                false,
                ex.getMessage(),
                new HashMap<>()
        );
    }

    @ExceptionHandler(RestaurantTableNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleRestaurantTableNotFoundException(
            RestaurantTableNotFoundException ex) {

        return new ErrorResponse(
                false,
                ex.getMessage(),
                new HashMap<>()
        );
    }

    @ExceptionHandler(DuplicateTableException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateTableException(
            DuplicateTableException ex) {

        ErrorResponse errorResponse = new ErrorResponse(
                false,
                ex.getMessage(),
                new HashMap<>()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleReservationNotFoundException(
            ReservationNotFoundException ex){

        return new ErrorResponse(
                false,
                ex.getMessage(),
                new HashMap<>()
        );
    }

    @ExceptionHandler(PartySizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlePartySizeExceededException(
            PartySizeExceededException ex){

        return new ErrorResponse(
                false,
                ex.getMessage(),
                new HashMap<>()
        );
    }

    @ExceptionHandler(DuplicateReservationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateReservationException(
            DuplicateReservationException ex){

        return new ErrorResponse(
                false,
                ex.getMessage(),
                new HashMap<>()
        );
    }

    @ExceptionHandler(InvalidReservationStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidReservationStatusException(
            InvalidReservationStatusException ex) {
        return new ErrorResponse(
                false,
                ex.getMessage(),
                new HashMap<>()
        );
    }

    @ExceptionHandler(MenuCategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleMenuCategoryNotFoundException(
            MenuCategoryNotFoundException ex) {

        return new ErrorResponse(
                false,
                ex.getMessage(),
                new HashMap<>()
        );
    }

    // =====================================================
    // ILLEGAL ARGUMENT EXCEPTION
    // =====================================================

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(
            IllegalArgumentException ex) {

        return new ErrorResponse(
                false,
                ex.getMessage(),
                new HashMap<>()
        );
    }


    // =====================================================
    // ILLEGAL STATE EXCEPTION
    // =====================================================

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIllegalStateException(
            IllegalStateException ex) {

        return new ErrorResponse(
                false,
                ex.getMessage(),
                new HashMap<>()
        );
    }

    // =====================================================
// GENERIC UNEXPECTED EXCEPTION
// =====================================================

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpectedException(
            Exception ex) {

        return new ErrorResponse(
                false,
                "An unexpected error occurred.",
                new HashMap<>()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(
            AccessDeniedException ex
    ) {

        return new ErrorResponse(
                false,
                ex.getMessage(),
                new HashMap<>()
        );
    }
}

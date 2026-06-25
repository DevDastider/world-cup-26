package org.sgd.worldcup.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.sgd.worldcup.dto.ApiResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(
            ResourceNotFoundException e, WebRequest request) {
        log.error("Resource not found: {}", e.getMessage());
        ApiResponse<?> response = ApiResponse.error(e.getMessage(), 404);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateResourceException(
            DuplicateResourceException e, WebRequest request) {
        log.error("Duplicate resource: {}", e.getMessage());
        ApiResponse<?> response = ApiResponse.error(e.getMessage(), 409);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidOperationException(
            InvalidOperationException e, WebRequest request) {
        log.error("Invalid operation: {}", e.getMessage());
        ApiResponse<?> response = ApiResponse.error(e.getMessage(), 400);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, WebRequest request) {
        log.error("Validation failed: {}", e.getBindingResult().getFieldError());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        String errorMessage = errors.values().stream()
                .collect(Collectors.joining(", "));

        ApiResponse<?> response = ApiResponse.error(errorMessage, 400);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(
            IllegalArgumentException e, WebRequest request) {
        log.error("Illegal argument: {}", e.getMessage());
        ApiResponse<?> response = ApiResponse.error(e.getMessage(), 400);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(
            Exception e, WebRequest request) {
        log.error("An unexpected error occurred: {}", e.getMessage(), e);
        ApiResponse<?> response = ApiResponse.error("An internal server error occurred", 500);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


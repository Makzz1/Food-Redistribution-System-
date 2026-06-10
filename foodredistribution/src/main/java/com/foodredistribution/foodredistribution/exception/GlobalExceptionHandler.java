package com.foodredistribution.foodredistribution.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Specific custom exceptions (declared BEFORE generic catches) ────────

    @ExceptionHandler(FoodNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFoodNotFoundException(FoodNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InsufficientQuantityException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientQuantityException(InsufficientQuantityException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedActionException(UnauthorizedActionException ex) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<Map<String, Object>> handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // ── Bean-validation failures (@Valid) → 400 ────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return buildError(HttpStatus.BAD_REQUEST, message);
    }

    // ── Business-rule violations (RuntimeException) → 400 ──────────────────

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── Catch-all fallback → 500 ────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // ── Shared builder ──────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
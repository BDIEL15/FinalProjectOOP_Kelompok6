package com.marsreboot.backend.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalidFields(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField).distinct().sorted().reduce((a, b) -> a + ", " + b)
                .map(fields -> "Periksa field: " + fields)
                .orElse("Data permintaan tidak valid");
        return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadableBody() {
        return ResponseEntity.badRequest().body(ApiError.of("INVALID_REQUEST", "Format isi permintaan tidak valid"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> statusException(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        String code = switch (status) {
            case BAD_REQUEST -> "INVALID_REQUEST";
            case UNAUTHORIZED -> "UNAUTHENTICATED";
            case FORBIDDEN -> "FORBIDDEN";
            case CONFLICT -> "CONFLICT";
            case NOT_FOUND -> "NOT_FOUND";
            default -> "REQUEST_FAILED";
        };
        String message = exception.getReason() == null ? "Permintaan tidak dapat diproses" : exception.getReason();
        return ResponseEntity.status(status).body(ApiError.of(code, message));
    }
}

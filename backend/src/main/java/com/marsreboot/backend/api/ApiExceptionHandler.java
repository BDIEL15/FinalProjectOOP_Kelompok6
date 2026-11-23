package com.marsreboot.backend.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/** Mengubah error dari controller menjadi respons JSON. */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Menyebutkan field yang datanya tidak valid. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalidFields(MethodArgumentNotValidException exception) {
        List<String> fieldNames = new ArrayList<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            String fieldName = error.getField();
            if (!fieldNames.contains(fieldName)) {
                fieldNames.add(fieldName);
            }
        }

        String message = fieldNames.isEmpty()
            ? "Data permintaan tidak valid"
            : "Periksa field: " + String.join(", ", fieldNames);

        return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_ERROR", message));
    }

    /** Menangani isi request yang bukan JSON valid. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadableBody() {
        return ResponseEntity.badRequest().body(ApiError.of("INVALID_REQUEST", "Format isi permintaan tidak valid"));
    }

    /** Mengubah status error aplikasi menjadi respons yang sesuai. */
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> statusException(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());

        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String code = toErrorCode(status);
        String message = exception.getReason();

        if (message == null) {
            message = "Permintaan tidak dapat diproses";
        }

        return ResponseEntity.status(status).body(ApiError.of(code, message));
    }

    /** Mengubah HTTP status menjadi kode error singkat yang mudah dibaca program */
    private String toErrorCode(HttpStatus status) {
        if (status == HttpStatus.BAD_REQUEST) {
            return "INVALID_REQUEST";
        }
        if (status == HttpStatus.UNAUTHORIZED) {
            return "UNAUTHENTICATED";
        }
        if (status == HttpStatus.FORBIDDEN) {
            return "FORBIDDEN";
        }
        if (status == HttpStatus.CONFLICT) {
            return "CONFLICT";
        }
        if (status == HttpStatus.NOT_FOUND) {
            return "NOT_FOUND";
        }
        return "REQUEST_FAILED";
    }
}

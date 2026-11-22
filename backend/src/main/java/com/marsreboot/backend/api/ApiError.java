package com.marsreboot.backend.api;

import java.util.UUID;

/** Bentuk jawaban error yang dikirim backend ke game. */
public class ApiError {

    private final String code;
    private final String message;
    private final String requestId;

    public ApiError(String code, String message, String requestId) {
        this.code = code;
        this.message = message;
        this.requestId = requestId;
    }

    public static ApiError of(String code, String message) {
        String newRequestId = UUID.randomUUID().toString();
        return new ApiError(code, message, newRequestId);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getRequestId() {
        return requestId;
    }
}

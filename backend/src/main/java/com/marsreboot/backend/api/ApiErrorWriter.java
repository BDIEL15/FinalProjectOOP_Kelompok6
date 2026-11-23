package com.marsreboot.backend.api;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class ApiErrorWriter {
    private ApiErrorWriter() {}

    public static void write(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");

        String requestId = UUID.randomUUID().toString();
        String json = "{\"code\":\"" + code
                + "\",\"message\":\"" + message
                + "\",\"requestId\":\"" + requestId + "\"}";

        response.getWriter().write(json);
    }
}

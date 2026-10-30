package br.oportunidades.cefet.backend.exceptions;

import java.time.Instant;

public record ErrorResponse(String message, int status, String path, Instant timestamp) {

    public static ErrorResponse of(String message, int status, String path) {
        return new ErrorResponse(message, status, path, Instant.now());
    }
}

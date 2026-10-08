package com.fastfood.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Duration;

/** Erreur métier renvoyée au client avec un code HTTP précis. */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    /** Pour une erreur 429 : délai conseillé avant de réessayer (en-tête Retry-After), sinon null. */
    private final Duration retryAfter;

    public ApiException(HttpStatus status, String message) {
        this(status, message, null);
    }

    private ApiException(HttpStatus status, String message, Duration retryAfter) {
        super(message);
        this.status = status;
        this.retryAfter = retryAfter;
    }

    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }

    public static ApiException tooManyRequests(String message, Duration retryAfter) {
        return new ApiException(HttpStatus.TOO_MANY_REQUESTS, message, retryAfter);
    }
}

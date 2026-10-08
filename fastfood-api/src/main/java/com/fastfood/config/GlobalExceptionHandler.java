package com.fastfood.config;

import com.fastfood.common.ApiException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/** Transforme les erreurs en réponses JSON lisibles (format RFC 9457). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> handleApi(ApiException ex) {
        var response = ResponseEntity.status(ex.getStatus());
        if (ex.getRetryAfter() != null) {
            // Arrondi à la seconde supérieure : on ne dit jamais de réessayer trop tôt
            long seconds = Math.max(1, (ex.getRetryAfter().toMillis() + 999) / 1000);
            response.header(HttpHeaders.RETRY_AFTER, String.valueOf(seconds));
        }
        return response.body(ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage()));
    }

    /** Levée par @Version quand deux personnes modifient la même donnée en même temps. */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleConcurrentUpdate(OptimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Cette donnée vient d'être modifiée par quelqu'un d'autre, rechargez et réessayez");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Données invalides");
        problem.setProperty("errors", errors);
        return problem;
    }
}

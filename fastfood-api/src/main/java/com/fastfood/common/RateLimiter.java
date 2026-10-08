package com.fastfood.common;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limiteur de tentatives par "fenêtre fixe" : au plus N actions par clé sur une période.
 * La clé identifie ce qu'on limite, par exemple "login-ip:203.0.113.4" ou "login-fail:a@b.fr".
 *
 * Les compteurs sont en mémoire : ils repartent à zéro au redémarrage et ne sont pas partagés
 * entre plusieurs instances. Suffisant pour un seul serveur ; avec plusieurs, il faudrait Redis.
 */
@Component
public class RateLimiter {

    private static final int CLEANUP_THRESHOLD = 10_000;

    private record Window(Instant resetAt, int count) {
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public RateLimiter(Clock clock) {
        this.clock = clock;
    }

    /** Compte une action, puis refuse (429) si la limite de la période est dépassée. */
    public void hit(String key, int limit, Duration period, String message) {
        Window window = increment(key, period);
        if (window.count() > limit) {
            throw ApiException.tooManyRequests(message, retryAfter(window));
        }
    }

    /** Refuse (429) si la limite est déjà atteinte, sans compter cette action. */
    public void check(String key, int limit, Duration period, String message) {
        Window window = current(key);
        if (window != null && window.count() >= limit) {
            throw ApiException.tooManyRequests(message, retryAfter(window));
        }
    }

    /** Compte une action sans rien vérifier (par exemple un mauvais mot de passe). */
    public void record(String key, Duration period) {
        increment(key, period);
    }

    public void reset(String key) {
        windows.remove(key);
    }

    private Window increment(String key, Duration period) {
        cleanUpIfNeeded();
        Instant now = clock.instant();
        return windows.compute(key, (k, w) ->
                w == null || !now.isBefore(w.resetAt())
                        ? new Window(now.plus(period), 1)
                        : new Window(w.resetAt(), w.count() + 1));
    }

    private Window current(String key) {
        Window window = windows.get(key);
        return window != null && clock.instant().isBefore(window.resetAt()) ? window : null;
    }

    private Duration retryAfter(Window window) {
        Duration remaining = Duration.between(clock.instant(), window.resetAt());
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    /** Évite que la mémoire grossisse sans fin si beaucoup de clés différentes sont utilisées. */
    private void cleanUpIfNeeded() {
        if (windows.size() > CLEANUP_THRESHOLD) {
            Instant now = clock.instant();
            windows.values().removeIf(w -> !now.isBefore(w.resetAt()));
        }
    }
}

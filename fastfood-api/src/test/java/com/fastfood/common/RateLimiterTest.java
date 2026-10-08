package com.fastfood.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimiterTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-08T10:00:00Z"));
    private final RateLimiter limiter = new RateLimiter(clock);
    private final Duration minute = Duration.ofMinutes(1);

    @Test
    void allowsUpToTheLimitThenRefusesWith429() {
        for (int i = 0; i < 3; i++) {
            limiter.hit("k", 3, minute, "trop");
        }

        assertThatThrownBy(() -> limiter.hit("k", 3, minute, "trop"))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                    assertThat(e.getRetryAfter()).isPositive();
                });
    }

    @Test
    void theCounterRestartsAfterThePeriod() {
        for (int i = 0; i < 3; i++) {
            limiter.hit("k", 3, minute, "trop");
        }
        clock.advance(minute);

        limiter.hit("k", 3, minute, "trop"); // ne lève pas d'exception
    }

    @Test
    void keysAreIndependent() {
        for (int i = 0; i < 3; i++) {
            limiter.hit("a", 3, minute, "trop");
        }
        limiter.hit("b", 3, minute, "trop"); // ne lève pas d'exception
    }

    @Test
    void checkBlocksOnceRecordedFailuresReachTheLimit() {
        limiter.check("fail", 2, minute, "bloqué");
        limiter.record("fail", minute);
        limiter.check("fail", 2, minute, "bloqué");
        limiter.record("fail", minute);

        assertThatThrownBy(() -> limiter.check("fail", 2, minute, "bloqué")).isInstanceOf(ApiException.class);
    }

    @Test
    void resetClearsTheCounter() {
        limiter.record("fail", minute);
        limiter.record("fail", minute);
        limiter.reset("fail");

        limiter.check("fail", 2, minute, "bloqué"); // ne lève pas d'exception
    }
}

package com.fastfood.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-123";

    @Test
    void aGeneratedTokenCanBeReadBack() {
        JwtService jwt = new JwtService(SECRET, 60);
        User user = new User();
        user.setId(42L);
        user.setEmail("client@test.fr");
        user.setRole(Role.CUSTOMER);

        AuthUser parsed = jwt.parse(jwt.generateToken(user)).orElseThrow();

        assertThat(parsed).isEqualTo(new AuthUser(42L, "client@test.fr", Role.CUSTOMER));
    }

    @Test
    void aTokenSignedWithAnotherKeyIsRejected() {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@b.fr");
        user.setRole(Role.RESTAURANT);
        String forged = new JwtService("another-secret-another-secret-another", 60).generateToken(user);

        assertThat(new JwtService(SECRET, 60).parse(forged)).isEmpty();
    }

    @Test
    void garbageIsRejected() {
        assertThat(new JwtService(SECRET, 60).parse("pas-un-jeton")).isEmpty();
    }
}

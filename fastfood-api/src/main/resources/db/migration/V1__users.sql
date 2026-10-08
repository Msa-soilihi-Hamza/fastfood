CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    loyalty_code  VARCHAR(6)   NOT NULL UNIQUE,
    points        INTEGER      NOT NULL DEFAULT 0 CHECK (points >= 0),
    version       BIGINT       NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

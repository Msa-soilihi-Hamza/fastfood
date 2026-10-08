ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Les comptes créés avant la vérification par e-mail restent utilisables
UPDATE users SET email_verified = TRUE;

-- Un "défi" = un code à 6 chiffres envoyé par e-mail, à saisir pour confirmer l'inscription ou la connexion
CREATE TABLE auth_challenges (
    id           UUID         PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    purpose      VARCHAR(20)  NOT NULL,
    code_hash    VARCHAR(100) NOT NULL,
    attempts     INTEGER      NOT NULL DEFAULT 0,
    send_count   INTEGER      NOT NULL DEFAULT 1,
    last_sent_at TIMESTAMPTZ  NOT NULL,
    expires_at   TIMESTAMPTZ  NOT NULL,
    consumed_at  TIMESTAMPTZ,
    version      BIGINT       NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_auth_challenges_user ON auth_challenges (user_id, purpose);

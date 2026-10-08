CREATE TABLE rewards (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    cost_points INTEGER      NOT NULL CHECK (cost_points > 0),
    active      BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Historique de chaque mouvement de points : gain (positif) ou cadeau (négatif)
CREATE TABLE point_transactions (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    delta      INTEGER      NOT NULL,
    reason     VARCHAR(200) NOT NULL,
    order_id   BIGINT REFERENCES orders (id),
    reward_id  BIGINT REFERENCES rewards (id),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_point_transactions_user ON point_transactions (user_id);

-- Une commande ne rapporte des points qu'une seule fois
CREATE UNIQUE INDEX uq_point_transactions_order ON point_transactions (order_id) WHERE order_id IS NOT NULL;

INSERT INTO rewards (name, cost_points) VALUES
    ('Boisson offerte',  30),
    ('Frites offertes',  40),
    ('Dessert offert',   50),
    ('Burger offert',   100);

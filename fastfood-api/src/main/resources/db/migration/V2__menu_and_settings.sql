CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    description VARCHAR(500),
    price       NUMERIC(8, 2) NOT NULL CHECK (price >= 0),
    category    VARCHAR(50)   NOT NULL,
    available   BOOLEAN       NOT NULL DEFAULT TRUE
);

-- Une seule ligne : les réglages du fast-food
CREATE TABLE restaurant_settings (
    id               BIGINT  PRIMARY KEY,
    takeaway_enabled BOOLEAN NOT NULL,
    dine_in_enabled  BOOLEAN NOT NULL,
    points_per_euro  INTEGER NOT NULL CHECK (points_per_euro >= 0)
);

INSERT INTO restaurant_settings (id, takeaway_enabled, dine_in_enabled, points_per_euro)
VALUES (1, TRUE, TRUE, 1);

INSERT INTO products (name, description, price, category) VALUES
    ('Burger classique',   'Steak, cheddar, salade, tomate, sauce maison', 8.50, 'Burgers'),
    ('Double cheese',      'Deux steaks, double cheddar, oignons',          10.90, 'Burgers'),
    ('Chicken burger',     'Poulet pané, salade, sauce blanche',             9.00, 'Burgers'),
    ('Frites',             'Portion moyenne',                                3.00, 'Accompagnements'),
    ('Nuggets x6',         'Avec une sauce au choix',                        5.50, 'Accompagnements'),
    ('Soda 33 cl',         NULL,                                             2.50, 'Boissons'),
    ('Eau 50 cl',          NULL,                                             1.80, 'Boissons'),
    ('Sundae caramel',     NULL,                                             3.50, 'Desserts');

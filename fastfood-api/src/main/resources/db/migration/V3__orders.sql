CREATE TABLE orders (
    id           BIGSERIAL PRIMARY KEY,
    customer_id  BIGINT        NOT NULL REFERENCES users (id),
    service_mode VARCHAR(20)   NOT NULL,
    status       VARCHAR(20)   NOT NULL,
    total        NUMERIC(8, 2) NOT NULL,
    version      BIGINT        NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_orders_status ON orders (status);

-- Le nom et le prix sont copiés : une commande passée ne change pas si le menu change
CREATE TABLE order_items (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT        NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id   BIGINT        NOT NULL REFERENCES products (id),
    product_name VARCHAR(100)  NOT NULL,
    unit_price   NUMERIC(8, 2) NOT NULL,
    quantity     INTEGER       NOT NULL CHECK (quantity > 0)
);

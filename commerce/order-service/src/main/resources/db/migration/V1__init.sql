CREATE TABLE IF NOT EXISTS orders
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_name  VARCHAR,
    customer_email VARCHAR,
    status         VARCHAR,
    status_details VARCHAR,
    created_at     TIMESTAMP,
    total_price DECIMAL
);

CREATE TABLE IF NOT EXISTS order_items
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id     BIGINT REFERENCES orders (id),
    product_id   BIGINT,
    product_name VARCHAR,
    quantity     INTEGER,
    price        DECIMAL
)
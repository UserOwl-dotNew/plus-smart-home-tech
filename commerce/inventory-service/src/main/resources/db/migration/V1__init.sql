CREATE TABLE IF NOT EXISTS inventories
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id        BIGINT,
    quantity          INTEGER DEFAULT 0,
    reserved_quantity INTEGER DEFAULT 0,
    version           BIGINT DEFAULT 0 NOT NULL
);
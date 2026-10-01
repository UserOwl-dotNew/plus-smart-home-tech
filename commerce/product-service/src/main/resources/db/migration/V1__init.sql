CREATE TABLE IF NOT EXISTS categories
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR,
    description VARCHAR
);

CREATE TABLE IF NOT EXISTS products
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR,
    description VARCHAR,
    price       DECIMAL,
    category_id BIGINT REFERENCES categories (id),
    image_url   VARCHAR,
    active      BOOLEAN
);
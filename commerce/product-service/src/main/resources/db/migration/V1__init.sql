CREATE TABLE IF NOT EXISTS categories
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR,
    description VARCHAR,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS products
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR,
    description VARCHAR,
    price       DECIMAL,
    category_id BIGINT REFERENCES categories (id),
    image_url   VARCHAR,
    active      BOOLEAN,
    PRIMARY KEY (id)
);
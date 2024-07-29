CREATE TABLE boost
(
    id               SMALLINT GENERATED ALWAYS AS IDENTITY,
    name             VARCHAR(255) NOT NULL,
    level            SMALLINT     NOT NULL,
    current_value    INTEGER      NOT NULL,
    condition_id     SMALLINT,
    price_to_upgrade BIGINT,
    max_level        BOOLEAN      NOT NULL,

    CONSTRAINT pk_boost PRIMARY KEY (id),
    CONSTRAINT unq_boost_name_level UNIQUE (name, level),
    CONSTRAINT chk_boost_max_level CHECK (
        (max_level IS TRUE AND price_to_upgrade IS NULL)
            OR
        (max_level IS FALSE AND price_to_upgrade IS NOT NULL)
        )
);
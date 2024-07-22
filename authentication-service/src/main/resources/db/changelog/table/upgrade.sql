CREATE TABLE upgrade
(
    name                  VARCHAR(255),
    section               VARCHAR(255) NOT NULL,
    level                 SMALLINT     NOT NULL,
    profit_per_hour       INTEGER      NOT NULL,
    profit_per_hour_delta INTEGER      NULL,
    price_to_upgrade      BIGINT       NULL,
    condition_id          SMALLINT     NULL,
    max_level             BOOLEAN      NOT NULL,
    --     available                  BOOLEAN      NOT NULL, -- ADD AS JAVA CONDITION

    CONSTRAINT pk_upgrade PRIMARY KEY (name, level),
--     CONSTRAINT fk_upgrade_condition_id FOREIGN KEY (condition_id) REFERENCES condition (id),
    CONSTRAINT chk_upgrade_max_level CHECK (
        (max_level IS TRUE AND profit_per_hour_delta IS NULL AND price_to_upgrade IS NULL)
            OR
        (max_level IS FALSE AND profit_per_hour_delta IS NOT NULL AND price_to_upgrade IS NOT NULL)
        )
);
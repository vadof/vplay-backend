CREATE TABLE upgrade
(
    name                  VARCHAR(255),
    section               VARCHAR(50) NOT NULL,
    level                 SMALLINT    NOT NULL,
    profit_per_hour       INTEGER     NOT NULL,
    profit_per_hour_delta INTEGER     NULL,
    price      INTEGER     NULL,
    condition_id          SMALLINT    NULL,
    max_level             BOOLEAN     NOT NULL,

    CONSTRAINT pk_upgrade PRIMARY KEY (name, level),
    CONSTRAINT fk_upgrade_section FOREIGN KEY (section) REFERENCES section (name),
    CONSTRAINT chk_upgrade_max_level CHECK (
        (max_level IS TRUE AND profit_per_hour_delta IS NULL AND price IS NULL)
            OR
        (max_level IS FALSE AND profit_per_hour_delta IS NOT NULL AND price IS NOT NULL)
        )
);
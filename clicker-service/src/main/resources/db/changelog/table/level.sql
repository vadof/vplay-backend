CREATE TABLE level
(
    value                SMALLINT    NOT NULL,
    name                 VARCHAR(20) NOT NULL,
    net_worth            BIGINT      NOT NULL,
    earn_per_tap         SMALLINT    NOT NULL,
    taps_recover_per_sec SMALLINT    NOT NULL,
    max_taps             SMALLINT    NOT NULL,

    CONSTRAINT pk_level PRIMARY KEY (value),
    CONSTRAINT unq_level_name UNIQUE (name),
    CONSTRAINT unq_net_worth UNIQUE (net_worth)
);
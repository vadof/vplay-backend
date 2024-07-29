CREATE TABLE level
(
    value     SMALLINT,
    name      VARCHAR(20) NOT NULL,
    net_worth BIGINT      NOT NULL,

    CONSTRAINT pk_level PRIMARY KEY (value),
    CONSTRAINT unq_level_name UNIQUE (name),
    CONSTRAINT unq_net_worth UNIQUE (net_worth)
);
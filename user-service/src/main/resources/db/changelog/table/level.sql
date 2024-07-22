CREATE TABLE level
(
    value     SMALLINT,
    name      VARCHAR(20) NOT NULL,
    net_worth BIGINT      NOT NULL,

    CONSTRAINT pk_level PRIMARY KEY (value)
);
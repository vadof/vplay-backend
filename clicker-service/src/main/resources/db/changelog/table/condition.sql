CREATE TABLE condition
(
    id           SMALLINT GENERATED ALWAYS AS IDENTITY,
    type         VARCHAR(50) NOT NULL,
    upgrade_name VARCHAR(50) NOT NULL,
    level        SMALLINT    NOT NULL,

    CONSTRAINT pk_condition PRIMARY KEY (id)
);
CREATE TABLE my_user
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY,
    firstname     VARCHAR(255) NOT NULL,
    lastname      VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    username      VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    register_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    country_code  VARCHAR(3)   NOT NULL,
    modified_at   TIMESTAMP    NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    frozen        BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_my_user PRIMARY KEY (id),
    CONSTRAINT fk_my_user_country_code FOREIGN KEY (country_code) REFERENCES country (code),
    CONSTRAINT unq_my_user_email UNIQUE (email),
    CONSTRAINT unq_my_user_username UNIQUE (username)
);
CREATE TABLE token
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    type        VARCHAR(30)  NOT NULL,
    options     VARCHAR(255) NULL,

    CONSTRAINT pk_token PRIMARY KEY (id),
    CONSTRAINT fk_token_user_id FOREIGN KEY (user_id) REFERENCES my_user (id),
    CONSTRAINT unq_token_user_id UNIQUE (user_id),
    CONSTRAINT unq_token_token_type UNIQUE (token, type)
);
CREATE TABLE refresh_token
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,

    CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT fk_refresh_token_user_id FOREIGN KEY (user_id) REFERENCES my_user (id),
    CONSTRAINT unq_refresh_token_user_id UNIQUE (user_id),
    CONSTRAINT unq_refresh_token_token UNIQUE (token)
);
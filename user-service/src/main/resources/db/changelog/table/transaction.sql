CREATE TABLE transaction
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id    BIGINT         NOT NULL,
    amount     DECIMAL(14, 2) NOT NULL,
    type       VARCHAR(50)    NOT NULL,
    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status     VARCHAR(20)    NOT NULL,

    CONSTRAINT pk_transaction PRIMARY KEY (id),
    CONSTRAINT fk_transaction_user_id FOREIGN KEY (user_id) REFERENCES my_user (id)
);
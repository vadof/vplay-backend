CREATE TABLE transaction
(
    event_id         UUID,
    amount           BIGINT      NOT NULL,
    bet_id           BIGINT      NOT NULL,
    transaction_type VARCHAR(10) NOT NULL,

    CONSTRAINT pk_transaction_event_id PRIMARY KEY (event_id),
    CONSTRAINT fk_transaction_user_id FOREIGN KEY (bet_id) REFERENCES bet (bet_id),
    CONSTRAINT unq_transaction_bet_id_transaction_type UNIQUE (bet_id, transaction_type)
)
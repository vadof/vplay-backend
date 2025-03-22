CREATE TABLE transaction
(
    event_id   UUID,
    amount     BIGINT NOT NULL,
    account_id BIGINT NOT NULL,

    CONSTRAINT pk_transaction_event_id PRIMARY KEY (event_id),
    CONSTRAINT fk_transaction_account_id FOREIGN KEY (account_id) REFERENCES account (id)
)
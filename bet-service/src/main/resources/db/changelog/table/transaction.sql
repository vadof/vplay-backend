CREATE TABLE transaction
(
    event_id UUID,
    amount   BIGINT NOT NULL,
    user_id  BIGINT NOT NULL,

    CONSTRAINT pk_transaction_event_id PRIMARY KEY (event_id),
    CONSTRAINT fk_transaction_user_id FOREIGN KEY (user_id) REFERENCES my_user (user_id)
)
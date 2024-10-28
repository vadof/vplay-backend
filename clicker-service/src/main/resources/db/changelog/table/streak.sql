CREATE TABLE streak
(
    account_id BIGINT NOT NULL,
    day SMALLINT NOT NULL,
    last_received_date DATE,

    CONSTRAINT pk_streak PRIMARY KEY (account_id),
    CONSTRAINT fk_streak_account_id FOREIGN KEY (account_id) REFERENCES account (id)
)
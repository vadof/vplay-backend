CREATE TABLE user_wallet
(
    user_id    BIGINT,
    balance    DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    updated_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_user_wallet PRIMARY KEY (user_id),
    CONSTRAINT fk_user_wallet_user_id FOREIGN KEY (user_id) REFERENCES my_user (id)
);
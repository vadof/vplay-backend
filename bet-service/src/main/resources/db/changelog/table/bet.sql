CREATE table bet
(
    bet_id     BIGINT GENERATED ALWAYS AS IDENTITY,
    market_id  BIGINT        NOT NULL,
    user_id    BIGINT        NOT NULL,
    odds       DECIMAL(4, 2) NOT NULL,
    created_at TIMESTAMP     NOT NULL,
    updated_at TIMESTAMP     NOT NULL,
    result     VARCHAR(20),

    CONSTRAINT pk_bet PRIMARY KEY (bet_id),
    CONSTRAINT fk_market_id FOREIGN KEY (market_id) REFERENCES market (market_id),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES my_user (user_id)
);

CREATE INDEX idx_bet_market_id on bet (market_id);
CREATE INDEX idx_bet_user_id on bet (user_id);

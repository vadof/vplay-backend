CREATE TABLE referral_bonus
(
    referral_bonus_id INTEGER GENERATED ALWAYS AS IDENTITY,
    referral_id       BIGINT NOT NULL,
    amount            DECIMAL(14, 2) NOT NULL,

    CONSTRAINT pk_referral_bonus PRIMARY KEY (referral_bonus_id),
    CONSTRAINT fk_referral_bonus_wallet FOREIGN KEY (referral_id) REFERENCES wallet (id),
    CONSTRAINT uq_referral_bonus_wallet UNIQUE (referral_id)
);
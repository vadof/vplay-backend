CREATE TABLE wallet
(
    id         BIGINT,
    balance    DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    reserved   DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    invited_by BIGINT         NULL,
    updated_at TIMESTAMP      NOT NULL,
    frozen     BOOLEAN        NOT NULL DEFAULT FALSE,
    version    INT            NOT NULL DEFAULT 0,

    CONSTRAINT pk_wallet PRIMARY KEY (id),
    CONSTRAINT fk_wallet_invited_bvy FOREIGN KEY (invited_by) REFERENCES wallet (id)
);
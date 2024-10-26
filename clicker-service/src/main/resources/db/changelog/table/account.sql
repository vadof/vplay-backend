CREATE TABLE account
(
    id                          BIGINT,
    level                       SMALLINT       NOT NULL DEFAULT 1,
    net_worth                   DECIMAL(21, 3) NOT NULL DEFAULT 0,
    balance_coins               DECIMAL(21, 3) NOT NULL DEFAULT 0,
    available_taps              SMALLINT       NOT NULL DEFAULT 100,
    max_taps                    SMALLINT       NOT NULL DEFAULT 100,
    earn_per_tap                SMALLINT       NOT NULL DEFAULT 1,
    taps_recover_per_sec        SMALLINT       NOT NULL DEFAULT 3,
    passive_earn_per_hour       INTEGER        NOT NULL DEFAULT 0,
    last_sync_date              TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    suspicious_actions_number   SMALLINT       NOT NULL DEFAULT 0,
    frozen                      BOOLEAN        NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_clicker_account PRIMARY KEY (id),
    CONSTRAINT chk_clicker_account_not_negative CHECK (
        level > 0 AND net_worth >= 0 AND balance_coins >= 0 AND available_taps >= 0
            AND max_taps >= 0 AND earn_per_tap >= 0 AND taps_recover_per_sec >= 0
            AND passive_earn_per_hour >= 0 AND suspicious_actions_number >= 0
        )
);
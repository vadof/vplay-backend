CREATE TABLE country
(
    code VARCHAR(3)   NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

INSERT INTO country (code, name)
VALUES ('EST', 'Estonia');
INSERT INTO country (code, name)
VALUES ('RUS', 'Russia');

CREATE TABLE my_user
(
    id            BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    firstname     VARCHAR(255)                 NOT NULL,
    lastname      VARCHAR(255)                 NOT NULL,
    email         VARCHAR(255)                 NOT NULL UNIQUE,
    username      VARCHAR(255)                 NOT NULL UNIQUE,
    password      VARCHAR(255)                 NOT NULL,
    register_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    country_code  VARCHAR(3) REFERENCES country (code),
    modified_at   TIMESTAMP NOT NULL,
    role          VARCHAR(20)                  NOT NULL,
    frozen        BOOLEAN DEFAULT FALSE        NOT NULL
);

CREATE TABLE user_wallet
(
    user_id    BIGINT PRIMARY KEY,
    balance    DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    updated_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_wallet_user_id FOREIGN KEY (user_id) REFERENCES my_user
);

CREATE TABLE transaction
(
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL, -- APPROVED, DECLINED

    CONSTRAINT fk_transaction_user_id FOREIGN KEY (user_id) REFERENCES my_user
);

CREATE TABLE refresh_token
(
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id     BIGINT       NOT NULL UNIQUE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP    NOT NULL,

    CONSTRAINT fk_refresh_token_user_id FOREIGN KEY (user_id) REFERENCES my_user
);

CREATE TABLE report
(
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_report_user_id FOREIGN KEY (user_id) REFERENCES my_user
);

CREATE TABLE clicker_account
(
    user_id BIGINT PRIMARY KEY,
    level SMALLINT NOT NULL DEFAULT 1,
    total_coins BIGINT NOT NULL DEFAULT 0,
    balance_coins BIGINT NOT NULL DEFAULT 0,
    available_taps SMALLINT NOT NULL DEFAULT 100,
    max_taps SMALLINT NOT NULL DEFAULT 100,
    earn_per_tap SMALLINT NOT NULL DEFAULT 1,
    taps_recover_per_sec SMALLINT NOT NULL DEFAULT 3,
    earn_passive_per_hour INTEGER NOT NULL DEFAULT 0,
    last_sync_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE upgrade
(
    id SMALLINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    section VARCHAR(255) NOT NULL,
    level SMALLINT NOT NULL, -- Current upgrade_level
    profit_per_hour INTEGER NOT NULL,
    user_level_to_upgrade SMALLINT,
    next_level_profit_per_hour INTEGER,
    price_to_upgrade BIGINT, -- Price to upgrade to the next level
    max_level boolean NOT NULL
--     CONSTRAINT ADD UNIQUE (name, level)
--     CONSTRAINT if level == max level, price_to_upgrade/next_level_profit_per_hour/level_to_upgrade can be null
);

INSERT INTO upgrade (name, section, level, profit_per_hour, user_level_to_upgrade, next_level_profit_per_hour, price_to_upgrade, max_level)
VALUES
('Telegram', 'Social Media', 0, 0, 1, 20, 1000, FALSE),
('Telegram', 'Social Media', 1, 20, 2, 50, 2000, FALSE),
('Telegram', 'Social Media', 2, 50, 5, 100, 5000, FALSE),
('Telegram', 'Social Media', 3, 100, 10, 200, 10000, FALSE),
('Telegram', 'Social Media', 4, 200, 20, 500, 20000, FALSE),
('Telegram', 'Social Media', 5, 500, 30, 1000, 50000, FALSE),
('Telegram', 'Social Media', 6, 1000, 40, 2000, 100000, FALSE),
('Telegram', 'Social Media', 7, 2000, 50, 5000, 200000, FALSE),
('Telegram', 'Social Media', 8, 5000, 75, 10000, 500000, FALSE),
('Telegram', 'Social Media', 9, 10000, 100, 50000, 1000000, FALSE),
('Telegram', 'Social Media', 10, 50000, null, null, null, TRUE);

CREATE TABLE user_upgrades
(
    user_id BIGINT NOT NULL,
    upgrade_id SMALLINT NOT NULL,

    CONSTRAINT fk_user_upgrades_user_id FOREIGN KEY (user_id) REFERENCES my_user,
    CONSTRAINT fk_user_upgrades_upgrade_id FOREIGN KEY (upgrade_id) REFERENCES upgrade,
    CONSTRAINT ADD PRIMARY KEY (user_id, upgrade_id) -- ADD NAME TO PK
);

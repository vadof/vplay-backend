CREATE TABLE reward
(
    id               INTEGER GENERATED ALWAYS AS IDENTITY,
    type             VARCHAR(50)  NOT NULL,
    name             VARCHAR(100) NOT NULL,
    link             VARCHAR(255) NULL,
    duration_seconds INTEGER      NULL,
    service_name     VARCHAR(50)  NOT NULL,
    reward_coins     INTEGER      NOT NULL,
    valid_from       TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ends_in          TIMESTAMP(0) NULL,

    CONSTRAINT pk_reward PRIMARY KEY (id),
    CONSTRAINT chk_reward_reward_coins_positive CHECK (reward_coins > 0),
    CONSTRAINT chk_reward_duration_seconds_positive CHECK (duration_seconds is NULL or duration_seconds > 0)
)

CREATE TABLE account_rewards_received
(
    account_id  BIGINT,
    reward_id   INTEGER,
    received_at TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    constraint pk_arr PRIMARY KEY (account_id, reward_id),
    constraint fk_arr_account_id FOREIGN KEY (account_id) REFERENCES account (id),
    constraint fk_arr_reward_id FOREIGN KEY (reward_id) REFERENCES reward (id)
)
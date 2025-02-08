CREATE TABLE account_task_reward_received
(
    account_id  BIGINT,
    task_id   INTEGER,
    received_at TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    constraint pk_arr PRIMARY KEY (account_id, task_id),
    constraint fk_arr_account_id FOREIGN KEY (account_id) REFERENCES account (id),
    constraint fk_arr_task_id FOREIGN KEY (task_id) REFERENCES task (id)
)
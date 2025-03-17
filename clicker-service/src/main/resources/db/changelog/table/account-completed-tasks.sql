CREATE TABLE account_completed_tasks
(
    account_id  BIGINT,
    task_id   INTEGER,
    completed_at TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    constraint pk_act PRIMARY KEY (account_id, task_id),
    constraint fk_act_account_id FOREIGN KEY (account_id) REFERENCES account (id),
    constraint fk_act_task_id FOREIGN KEY (task_id) REFERENCES task (id)
)
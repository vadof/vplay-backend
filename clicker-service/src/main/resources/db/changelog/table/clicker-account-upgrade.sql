CREATE TABLE account_upgrade
(
    account_id    BIGINT       NOT NULL,
    upgrade_name  VARCHAR(255) NOT NULL,
    upgrade_level SMALLINT     NOT NULL,

    CONSTRAINT pk_account_upgrade_account_id_upgrade_name_level PRIMARY KEY (account_id, upgrade_name),
    CONSTRAINT fk_account_upgrade_account_id FOREIGN KEY (account_id) REFERENCES account (id),
    CONSTRAINT fk_account_upgrade_upgrade_name FOREIGN KEY (upgrade_name, upgrade_level) REFERENCES upgrade (name, level)
);
CREATE TABLE user_upgrade
(
    user_id       BIGINT       NOT NULL,
    upgrade_name  VARCHAR(255) NOT NULL,
    upgrade_level SMALLINT     NOT NULL,

    CONSTRAINT pk_user_upgrade_user_id_upgrade_name_level PRIMARY KEY (user_id, upgrade_name),
    CONSTRAINT fk_user_upgrade_user_id FOREIGN KEY (user_id) REFERENCES my_user (id),
    CONSTRAINT fk_user_upgrade_upgrade_name FOREIGN KEY (upgrade_name, upgrade_level) REFERENCES upgrade (name, level)
);
ALTER TABLE upgrade
    ADD CONSTRAINT fk_upgrade_condition_id FOREIGN KEY (condition_id) REFERENCES condition (id);
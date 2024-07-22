ALTER TABLE condition
    ADD CONSTRAINT fk_condition_upgrade_name_level FOREIGN KEY (upgrade_name, level)
        REFERENCES upgrade (name, level);
ALTER TABLE boost
    ADD CONSTRAINT fk_boost_condition_id FOREIGN KEY (condition_id) REFERENCES condition (id);
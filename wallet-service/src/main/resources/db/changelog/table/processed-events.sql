CREATE TABLE processed_events
(
    event_id     UUID,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_processed_events PRIMARY KEY (event_id)
);

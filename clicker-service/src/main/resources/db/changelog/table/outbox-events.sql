CREATE TABLE outbox_events
(
    id           UUID,
    aggregate_id BIGINT       NOT NULL,
    event_type   VARCHAR(255) NOT NULL,
    payload      TEXT         NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at  TIMESTAMP,

    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);

CREATE INDEX idx_outbox_events_type_status ON outbox_events (event_type, status);
CREATE INDEX idx_outbox_events_aggregate_id ON outbox_events (aggregate_id);
CREATE INDEX idx_outbox_events_modified_at ON outbox_events (modified_at);
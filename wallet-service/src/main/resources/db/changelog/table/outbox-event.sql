CREATE TABLE outbox_event
(
    id           UUID,
    aggregate_id BIGINT       NOT NULL,
    type         VARCHAR(100) NOT NULL,
    payload      TEXT         NOT NULL,
    status       VARCHAR(50)  NOT NULL,
    applicant    VARCHAR(100) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at  TIMESTAMP,
    version      INT          NOT NULL DEFAULT 0,

    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);

CREATE INDEX idx_outbox_event_status_created_at ON outbox_event (status, created_at);
CREATE INDEX idx_outbox_event_aggregate_id ON outbox_event (aggregate_id);

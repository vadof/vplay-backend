CREATE TABLE report
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id     BIGINT    NULL,
    description TEXT      NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_report PRIMARY KEY (id),
    CONSTRAINT fk_report_user_id FOREIGN KEY (user_id) REFERENCES my_user (id)
);
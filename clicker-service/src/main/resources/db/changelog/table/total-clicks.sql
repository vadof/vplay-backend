CREATE TABLE total_clicks
(
    date   TIMESTAMP,
    amount BIGINT NOT NULL,
    CONSTRAINT pk_total_clicks PRIMARY KEY (date)
)
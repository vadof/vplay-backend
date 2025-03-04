CREATE TABLE account_clicks
(
    account_id BIGINT NOT NULL,
    date       DATE   NOT NULL,
    amount     INT    NOT NULL,
    CONSTRAINT pk_account_clicks PRIMARY KEY (account_id, date)
);

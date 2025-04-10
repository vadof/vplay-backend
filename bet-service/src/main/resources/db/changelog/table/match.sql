CREATE table match
(
    match_id         BIGINT GENERATED ALWAYS AS IDENTITY,
    tournament_id    INTEGER       NOT NULL,
    match_page       VARCHAR(255)  NOT NULL,
    participant1_id  INTEGER       NOT NULL,
    participant2_id  INTEGER       NOT NULL,
    format           VARCHAR(20)   NOT NULL,
    start_date       TIMESTAMP(0)  NOT NULL,
    status           VARCHAR(50)   NOT NULL,
    winner           SMALLINT      NULL, -- 1 or 2
    win_probability1 DECIMAL(3, 2) NULL,
    win_probability2 DECIMAL(3, 2) NULL,
    created_at       TIMESTAMP(0)  NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
    modified_at      TIMESTAMP(0)  NULL,

    CONSTRAINT pk_match PRIMARY KEY (match_id),
    CONSTRAINT fk_match_tournament FOREIGN KEY (tournament_id) REFERENCES tournament (tournament_id),
    CONSTRAINT fk_match_participant1 FOREIGN KEY (participant1_id) REFERENCES participant (participant_id),
    CONSTRAINT fk_match_participant2 FOREIGN KEY (participant2_id) REFERENCES participant (participant_id),
    CONSTRAINT unq_match_match_page UNIQUE (match_page),
    CONSTRAINT chk_match_participants CHECK (participant1_id <> participant2_id),
    CONSTRAINT chk_win_probability CHECK (win_probability1 > 0 AND win_probability2 > 0 AND win_probability1 + win_probability2 = 1)
);

CREATE INDEX idx_match_tournament ON match (tournament_id);
CREATE INDEX idx_match_start_date_status ON match (start_date, status);

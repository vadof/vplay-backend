CREATE TABLE match_map
(
    match_id           BIGINT,
    map_number         SMALLINT,
    map_name           VARCHAR(100)  NULL,
    participant1_score SMALLINT      NOT NULL,
    participant2_score SMALLINT      NOT NULL,
    winner             SMALLINT, -- 1 or 2
    initial_wp_1       DECIMAL(4, 3) NOT NULL,
    initial_wp_2       DECIMAL(4, 3) NOT NULL,
    current_wp_1       DECIMAL(4, 3) NOT NULL,
    current_wp_2       DECIMAL(4, 3) NOT NULL,
    updated_at         TIMESTAMP(0)  NULL,

    CONSTRAINT pk_match_map PRIMARY KEY (match_id, map_number),
    CONSTRAINT fk_match_map_match FOREIGN KEY (match_id) REFERENCES match (match_id)
);

CREATE INDEX idx_match_map_match ON match_map (match_id);
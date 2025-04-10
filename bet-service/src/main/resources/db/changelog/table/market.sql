CREATE TABLE market
(
    market_id   BIGINT GENERATED ALWAYS AS IDENTITY,
    match_id    BIGINT        NOT NULL,
    participant SMALLINT,
    map_number  SMALLINT,
    outcome     DECIMAL(3, 1) NOT NULL,
    odds        DECIMAL(4, 2) NOT NULL,
    closed      BOOLEAN       NOT NULL,
    result      VARCHAR(10),
    updated_at  TIMESTAMP     NOT NULL,
    created_at  TIMESTAMP     NOT NULL,
    dtype       VARCHAR(100)  NOT NULL,

    CONSTRAINT pk_market PRIMARY KEY (market_id),
    CONSTRAINT fk_market_match_id FOREIGN KEY (match_id) REFERENCES match (match_id),
    CONSTRAINT chk_market_winner_map CHECK (dtype != 'WinnerMap' OR map_number IS NOT NULL),
    CONSTRAINT chk_market_total_map_rounds CHECK (dtype != 'TotalMapRounds' OR map_number IS NOT NULL),
    CONSTRAINT chk_market_handicap_maps CHECK (dtype != 'HandicapMaps' OR participant IS NOT NULL)
);

CREATE INDEX idx_odds_match_id ON market (match_id);
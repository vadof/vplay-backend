CREATE TABLE tournament
(
    tournament_id   INTEGER GENERATED ALWAYS AS IDENTITY,
    title           VARCHAR(100) NOT NULL,
    discipline      VARCHAR(50)  NOT NULL,
    tournament_page VARCHAR(255) NULL,
    image_id        BIGINT       NULL,
    start_date      TIMESTAMP(0) NOT NULL,
    end_date        TIMESTAMP(0) NOT NULL,
    created_at      TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
    modified_at     TIMESTAMP(0) NULL,

    CONSTRAINT pk_tournament PRIMARY KEY (tournament_id),
    CONSTRAINT fk_tournament_image FOREIGN KEY (image_id) REFERENCES image (image_id),
    CONSTRAINT unq_title_discipline UNIQUE (title, discipline),
    CONSTRAINT chk_tournament_date_range CHECK (start_date < end_date)
);

CREATE INDEX idx_tournament_date_range ON tournament (start_date, end_date);
CREATE INDEX idx_tournament_title ON tournament (title);
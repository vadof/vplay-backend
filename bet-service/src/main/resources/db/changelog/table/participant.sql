CREATE TABLE participant
(
    participant_id   INTEGER GENERATED ALWAYS AS IDENTITY,
    name             VARCHAR(100) NOT NULL,
    short_name       VARCHAR(100) NOT NULL, -- Set name if null
    discipline       VARCHAR(50)  NOT NULL,
    image_id         BIGINT       NULL,
    participant_page VARCHAR(255) NULL,
    created_at       TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
    modified_at      TIMESTAMP(0) NULL,

    CONSTRAINT pk_participant PRIMARY KEY (participant_id),
    CONSTRAINT fk_participant_image FOREIGN KEY (image_id) REFERENCES image (image_id),
    CONSTRAINT unq_participant_name_discipline UNIQUE (name, discipline)
);


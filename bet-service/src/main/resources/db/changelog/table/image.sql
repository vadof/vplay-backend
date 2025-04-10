CREATE TABLE image
(
    image_id    BIGINT GENERATED ALWAYS AS IDENTITY,
    file_path   VARCHAR(255) NOT NULL,

    CONSTRAINT pk_image PRIMARY KEY (image_id)
);
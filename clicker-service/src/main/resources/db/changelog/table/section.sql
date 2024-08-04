CREATE TABLE section
(
    id SMALLINT GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(50) NOT NULL,

    CONSTRAINT pk_section PRIMARY KEY (id),
    CONSTRAINT unq_section_name UNIQUE (name)
);
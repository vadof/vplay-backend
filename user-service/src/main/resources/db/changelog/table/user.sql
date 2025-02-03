CREATE TABLE my_user
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY,
    name              VARCHAR(100),
    email             VARCHAR(255),
    username          VARCHAR(255),
    password          VARCHAR(255),
    oauth_provider    VARCHAR(50),
    oauth_provider_id VARCHAR(255),
    register_date     TIMESTAMP(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at       TIMESTAMP(0),
    role              VARCHAR(20)  NOT NULL,
    active            BOOLEAN      NOT NULL,
    frozen            BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_my_user PRIMARY KEY (id),
    CONSTRAINT unq_my_user_username UNIQUE (username),
    CONSTRAINT unq_my_user_oauth_provider_id UNIQUE (oauth_provider, oauth_provider_id)
);

CREATE INDEX idx_my_user_email ON my_user (email);
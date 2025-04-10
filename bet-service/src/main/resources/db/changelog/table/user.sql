CREATE TABLE my_user
(
    user_id BIGINT,
    frozen BOOLEAN default FALSE,

    CONSTRAINT pk_bettor PRIMARY KEY (user_id)
)
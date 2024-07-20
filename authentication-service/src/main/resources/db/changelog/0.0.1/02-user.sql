CREATE TABLE my_user
(
    id            BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    firstname     VARCHAR(255)              NOT NULL,
    lastname      VARCHAR(255)              NOT NULL,
    email         VARCHAR(255)              NOT NULL UNIQUE,
    username      VARCHAR(255)              NOT NULL UNIQUE,
    password      VARCHAR(255)              NOT NULL,
    register_date DATE DEFAULT CURRENT_DATE NOT NULL,
    country_code  VARCHAR(3) REFERENCES country (code),
    role          VARCHAR(20)               NOT NULL
);

CREATE TABLE country
(
    code VARCHAR(3)   NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

INSERT INTO country (code, name) VALUES ('EST', 'Estonia');
INSERT INTO country (code, name) VALUES ('RUS', 'Russia');

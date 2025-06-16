DROP TABLE endpointhits;

CREATE TABLE IF NOT EXISTS endpointhits
(
    id        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app       VARCHAR(128),
    uri       VARCHAR(128),
    ip        VARCHAR(128),
    timestamp TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS categories
(
    id   INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(128)                             NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (id),
    CONSTRAINT UQ_CATEGORY UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS users
(
    id    INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name  VARCHAR(255)                             NOT NULL,
    email VARCHAR(255)                             NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS events
(
    id                INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    annotation        VARCHAR(2000),
    category_id       INTEGER,
    createdOn         TIMESTAMP WITHOUT TIME ZONE,
    description       VARCHAR(7000),
    eventDate         TIMESTAMP WITHOUT TIME ZONE,
    initiator_id      INTEGER                                  NOT NULL,
    lat               FLOAT,
    lon               FLOAT,
    paid              BOOLEAN,
    participantLimit  INTEGER,
    publishedOn       TIMESTAMP WITHOUT TIME ZONE,
    requestModeration BOOLEAN,
    state             VARCHAR(32),
    title             VARCHAR(128),
    CONSTRAINT pk_event PRIMARY KEY (id),
    CONSTRAINT fk_events_to_users FOREIGN KEY (initiator_id) REFERENCES users (id),
    CONSTRAINT fk_events_to_categories FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE IF NOT EXISTS requests
(
    id           INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    requester_id INTEGER                                  NOT NULL,
    event_id     INTEGER                                  NOT NULL,
    status       VARCHAR(32),
    created      TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_request PRIMARY KEY (id),
    CONSTRAINT fk_requests_to_users FOREIGN KEY (requester_id) REFERENCES users (id),
    CONSTRAINT fk_requests_to_events FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT unique_requester_event UNIQUE (requester_id, event_id)
);

CREATE TABLE IF NOT EXISTS compilations
(
    id     INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    title  VARCHAR(128),
    pinned BOOLEAN,
    CONSTRAINT pk_compilation PRIMARY KEY (id),
    CONSTRAINT UQ_COMPILATION_TITLE UNIQUE (title)
);


CREATE TABLE IF NOT EXISTS eventlinks
(
    event_id       INTEGER NOT NULL,
    compilation_id INTEGER NOT NULL,
    CONSTRAINT pk_eventlinks PRIMARY KEY (event_id, compilation_id),
    CONSTRAINT fk_links_to_events FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_links_to_compilations FOREIGN KEY (compilation_id) REFERENCES compilations (id)
);

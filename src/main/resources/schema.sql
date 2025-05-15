CREATE TABLE IF NOT EXISTS mpa
(
    id   BIGINT PRIMARY KEY,
    name VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS genres
(
    id   BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS users
(
    id       BIGINT PRIMARY KEY,
    name     VARCHAR(100),
    email    VARCHAR(100) NOT NULL,
    login    VARCHAR(100) NOT NULL,
    birthday DATE
);

CREATE TABLE IF NOT EXISTS friends
(
    user_id   BIGINT REFERENCES users (id),
    friend_id BIGINT REFERENCES users (id),
    status    BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS films
(
    id           BIGINT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  VARCHAR(200),
    release_date DATE         NOT NULL,
    duration     INT          NOT NULL,
    mpa_id       BIGINT REFERENCES mpa (id)
);

CREATE TABLE IF NOT EXISTS film_likes
(
    film_id BIGINT REFERENCES films (id),
    user_id BIGINT REFERENCES users (id),
    PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS film_genres
(
    film_id  BIGINT REFERENCES films (id),
    genre_id BIGINT REFERENCES genres (id),
    PRIMARY KEY (film_id, genre_id)
);
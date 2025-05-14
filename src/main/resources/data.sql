DELETE FROM film_likes;
DELETE FROM friends;
DELETE FROM film_genres;
DELETE FROM users;
DELETE FROM films;
DELETE FROM mpa;
DELETE FROM genres;

ALTER TABLE `users` ALTER COLUMN `id` RESTART WITH 1;
ALTER TABLE `films` ALTER COLUMN `id` RESTART WITH 1;
ALTER TABLE `genres` ALTER COLUMN `id` RESTART WITH 1;
ALTER TABLE `mpa` ALTER COLUMN `id` RESTART WITH 1;

INSERT INTO `genres` (`name`)
VALUES
    ('Комедия'),
    ('Драма'),
    ('Мультфильм'),
    ('Триллер'),
    ('Документальный'),
    ('Боевик');

INSERT INTO `mpa` (`name`)
VALUES
    ('G'),
    ('PG'),
    ('PG-13'),
    ('R'),
    ('NC-17');

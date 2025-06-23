DELETE FROM events;
DELETE FROM film_likes;
DELETE FROM friends;
DELETE FROM film_genres;
DELETE from film_director;
DELETE FROM review_votes;
DELETE FROM reviews;
DELETE FROM users;
DELETE FROM films;
DELETE FROM mpa;
DELETE FROM genres;
DELETE from directors;

ALTER TABLE `events` ALTER COLUMN `event_id` RESTART WITH 1;
ALTER TABLE `users` ALTER COLUMN `id` RESTART WITH 1;
ALTER TABLE `films` ALTER COLUMN `id` RESTART WITH 1;
ALTER TABLE `genres` ALTER COLUMN `id` RESTART WITH 1;
ALTER TABLE `mpa` ALTER COLUMN `id` RESTART WITH 1;
ALTER TABLE `reviews` ALTER COLUMN `review_id` RESTART WITH 1;
ALTER TABLE `directors` ALTER COLUMN `id` RESTART WITH 1;

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

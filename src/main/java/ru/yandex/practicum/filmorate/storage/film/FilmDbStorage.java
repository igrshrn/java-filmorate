package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.film.FilmResultSetExtractor;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    public FilmDbStorage(JdbcTemplate jdbc, FilmResultSetExtractor extractor) {
        super(jdbc, extractor);
        log.info("FilmResultSetExtractor initialized: {}", extractor != null);
    }

    private static final String FILM_COLUMNS = """
            f.id AS film_id,
            f.name AS film_name,
            f.description AS film_description,
            f.release_date AS film_release_date,
            f.duration AS film_duration,
            m.id AS mpa_id,
            m.name AS mpa_name,
            g.id AS genre_id,
            g.name AS genre_name,
            fl.user_id AS like_user_id""";

    private static final String FILM_JOIN = """
            JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN film_genres fg ON f.id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id""";

    private static final String BASE_SELECT = """
            SELECT %s
            FROM films f
            %s""".formatted(FILM_COLUMNS, FILM_JOIN);

    private static final String FIND_ALL = BASE_SELECT;
    private static final String FIND_BY_ID = BASE_SELECT + " WHERE f.id = ?";

    private static final String INSERT = """
            INSERT INTO films (
                name,
                description,
                release_date,
                duration,
                mpa_id
            ) VALUES (?, ?, ?, ?, ?)""";

    private static final String UPDATE = """
            UPDATE films
            SET
                name = ?,
                description = ?,
                release_date = ?,
                duration = ?,
                mpa_id = ?
            WHERE id = ?""";

    private static final String DELETE_FILM = "DELETE FROM films WHERE id = ?";
    private static final String DELETE_GENRES = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String INSERT_GENRE = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String INSERT_LIKE = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String DELETE_LIKES = "DELETE FROM film_likes WHERE film_id = ?";

    private static final String POPULAR_SUBQUERY = """
            SELECT f.id AS film_id, COUNT(fl.user_id) AS like_count
            FROM films f
                LEFT JOIN film_likes fl ON f.id = fl.film_id
            GROUP BY f.id
            ORDER BY like_count DESC
            LIMIT ?""";

    private static final String FIND_POPULAR = """
            SELECT
                f.id AS film_id,
                f.name AS film_name,
                f.description AS film_description,
                f.release_date AS film_release_date,
                f.duration AS film_duration,
                m.id AS mpa_id,
                m.name AS mpa_name,
                g.id AS genre_id,
                g.name AS genre_name,
                fl.user_id AS like_user_id,
                l.like_count
            FROM(%s) as l
            LEFT JOIN FILMS f on l.film_id = f.id
            LEFT JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN film_genres fg ON f.id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            ORDER BY l.like_count DESC""".formatted(POPULAR_SUBQUERY);

    @Override
    public Film create(Film film) {
        long id = insert(INSERT,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);
        insertGenres(film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        update(UPDATE,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        updateGenres(film);
        log.info("Обновлен фильм: {}", film);
        return film;
    }

    @Override
    public Optional<Film> getFilmById(long id) {
        return findOne(FIND_BY_ID, id);
    }

    @Override
    public Collection<Film> getAll() {
        return findMany(FIND_ALL);
    }

    @Override
    public void delete(long id) {
        delete(DELETE_LIKES, id);
        delete(DELETE_GENRES, id);
        delete(DELETE_FILM, id);
        log.info("Удален фильм с ID {}", id);
    }

    @Override
    public Collection<FilmDto> getPopularFilms(int count) {
        Map<Long, FilmDto> filmMap = new LinkedHashMap<>();

        jdbc.query(FIND_POPULAR, (rs) -> {
            while (rs.next()) {

                long filmId = rs.getLong("film_id");
                FilmDto film = filmMap.computeIfAbsent(filmId, k -> {
                    try {
                        return FilmDto.builder()
                                .id(rs.getLong("film_id"))
                                .name(rs.getString("film_name"))
                                .description(rs.getString("film_description"))
                                .releaseDate(rs.getDate("film_release_date").toLocalDate())
                                .duration(rs.getInt("film_duration"))
                                .mpa(Mpa.builder().build())
                                .genres(new HashSet<>())
                                .likes(new HashSet<>())
                                .likesCount(rs.getLong("like_count"))
                                .build();
                    } catch (SQLException e) {
                        throw new RuntimeException("Ошибка маппинга", e);
                    }
                });
                film.setMpa(Mpa.builder()
                        .id(rs.getLong("mpa_id"))
                        .name(rs.getString("mpa_name"))
                        .build());

                Long genreId = rs.getObject("genre_id", Long.class);
                if (genreId != null && genreId != 0) {
                    film.getGenres().add(Genre.builder()
                            .id(genreId)
                            .name(rs.getString("genre_name"))
                            .build());
                }

                Long userId = rs.getObject("user_id", Long.class);
                if (userId != null && userId != 0) {
                    film.getLikes().add(userId);
                }
                Long likeCount = rs.getObject("like_count", Long.class);
                if (likeCount != null && likeCount != 0) {
                    film.getLikes().add(userId);
                }
            }
        }, count);

        return filmMap.values();
    }

    @Override
    public void addLike(long filmId, long userId) {
        update(INSERT_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        update(DELETE_LIKE, filmId, userId);
    }

    private void updateGenres(Film film) {
        update(DELETE_GENRES, film.getId());
        insertGenres(film);
    }

    private void insertGenres(Film film) {
        if (film.getGenres() == null) {
            return;
        }
        List<Object[]> batch = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .collect(Collectors.toList());

        jdbc.batchUpdate(INSERT_GENRE, batch, batch.size(), (ps, args) -> {
            ps.setLong(1, (Long) args[0]);
            ps.setLong(2, (Long) args[1]);
        });

        batch.forEach(args -> log.info("Добавлен жанр {} к фильму: {}", args[1], args[0]));
    }
}

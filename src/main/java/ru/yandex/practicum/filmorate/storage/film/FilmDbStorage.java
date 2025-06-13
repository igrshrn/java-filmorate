package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.film.FilmResultSetExtractor;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Director;
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
            fl.user_id AS like_user_id,
            d.id AS director_id,
            d.name AS director_name
            """;

    private static final String FILM_JOIN = """
            JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN film_genres fg ON f.id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            LEFT JOIN film_director fd ON f.id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.id
            """;

    private static final String BASE_SELECT = """
            SELECT %s
            FROM films f
            %s""".formatted(FILM_COLUMNS, FILM_JOIN);

    private static final String FIND_ALL = BASE_SELECT;
    private static final String FIND_BY_ID = BASE_SELECT + " WHERE f.id = ?";
    private static final String FIND_BY_DIRECTOR_ID = """
            SELECT f.id AS film_id,
            f.name AS film_name,
            f.description AS film_description,
            f.release_date AS film_release_date,
            f.duration AS film_duration,
            f.mpa_id,
            m.name AS mpa_name,
            g.id AS genre_id,
            g.name AS genre_name,
            fl.user_id AS user_id,
            d.id AS director_id,
            d.name AS director_name,
            COUNT(fl.user_id) AS like_count
            FROM films f
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN film_genres fg ON f.id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.id
            LEFT JOIN film_director fd ON f.id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.id
            WHERE f.id IN (
            SELECT fd.film_id FROM film_director fd WHERE fd.director_id = ?)
            GROUP BY f.id,fl.USER_ID""";

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
    private static final String DELETE_DIRECTOR = "DELETE FROM film_director WHERE film_id = ?";
    private static final String INSERT_DIRECTOR = "INSERT INTO film_director (film_id, director_id) VALUES (?,?)";

    private static final String POPULAR_SUBQUERY = """
            SELECT f.id AS film_id, COUNT(fl.user_id) AS like_count
            FROM films f
                LEFT JOIN film_likes fl ON f.id = fl.film_id
            GROUP BY f.id
            ORDER BY like_count DESC
            LIMIT ?""";

    private static final String FIND_POPULAR_WITHOUT_GENRE_YEAR = """
            SELECT %s, l.like_count
            FROM(%s) as l
            LEFT JOIN FILMS f on l.film_id = f.id
            LEFT %s
            ORDER BY l.like_count DESC""".formatted(FILM_COLUMNS,POPULAR_SUBQUERY, FILM_JOIN);

    private static final String FIND_POPULAR_WITH_YEAR = """
        SELECT film_data.*,
            fl.user_id AS like_user_id
        FROM (
            SELECT
                f.id AS film_id,
                f.name AS film_name,
                f.description AS film_description,
                f.release_date AS film_release_date,
                EXTRACT(YEAR FROM f.release_date) AS release_year,
                f.duration AS film_duration,
                m.id AS mpa_id,
                m.name AS mpa_name,
                g.id AS genre_id,
                g.name AS genre_name,
                d.id AS director_id,
                d.name AS director_name,
                COUNT(DISTINCT fl.user_id) AS like_count
            FROM films f
            LEFT %s
            WHERE EXTRACT(YEAR FROM f.release_date) = ?
            GROUP BY
                f.id, f.name, f.description,
                f.release_date, f.duration,
                m.id, m.name,
                g.id, g.name,
                d.id, d.name
            ORDER BY like_count DESC
            LIMIT ?
        ) film_data
        LEFT JOIN film_likes fl ON film_data.film_id = fl.film_id
        ORDER BY film_data.like_count DESC, film_data.film_id, fl.user_id""".formatted(FILM_JOIN);

    private static final String FIND_POPULAR_WITH_GENRE_YEAR = """
            SELECT
            f.id AS film_id,
            f.name AS film_name,
            f.description AS film_description,
            f.release_date AS film_release_date,
            EXTRACT(YEAR FROM f.release_date) AS release_year,
            f.duration AS film_duration,
            m.id AS mpa_id,
            m.name AS mpa_name,
            g.id AS genre_id,
            g.name AS genre_name,
            fl.user_id AS like_user_id,
            d.id AS director_id,
            d.name AS director_name,
            COUNT(fl.user_id) AS like_count
            FROM films f
            LEFT %s
            WHERE g.id = ? AND EXTRACT(YEAR FROM f.release_date) = ?
            GROUP BY
            f.id, f.name, f.description,
            f.release_date, f.duration,
            m.id, m.name,
            g.id, g.name,
            d.id, d.name,
            fl.user_id
            ORDER BY like_count DESC
            LIMIT ?""".formatted(FILM_JOIN);

    private static final String FIND_POPULAR_WITH_GENRE = """
                    SELECT film_data.*,
                        fl.user_id AS like_user_id
                    FROM (
                        SELECT
                            f.id AS film_id,
                            f.name AS film_name,
                            f.description AS film_description,
                            f.release_date AS film_release_date,
                            EXTRACT(YEAR FROM f.release_date) AS release_year,
                            f.duration AS film_duration,
                            m.id AS mpa_id,
                            m.name AS mpa_name,
                            g.id AS genre_id,
                            g.name AS genre_name,
                            d.id AS director_id,
                            d.name AS director_name,
                            COUNT(DISTINCT fl.user_id) AS like_count
                        FROM films f
                        LEFT %s
                        WHERE g.id = ?
                        GROUP BY
                            f.id, f.name, f.description,
                            f.release_date, f.duration,
                            m.id, m.name,
                            g.id, g.name,
                            d.id, d.name
                        ORDER BY like_count DESC
                        LIMIT ?
                    ) film_data
                    LEFT JOIN film_likes fl ON film_data.film_id = fl.film_id
                    ORDER BY film_data.like_count DESC, film_data.film_id, fl.user_id""".formatted(FILM_JOIN);

    private static final String SEARCH_FILMS = """
            SELECT %s
            FROM films f
            %s
            WHERE 1=0
            %s
            GROUP BY f.id, m.id, g.id, d.id
            ORDER BY COUNT(DISTINCT fl.user_id) DESC
            """.formatted(FILM_COLUMNS, FILM_JOIN, "%s");

    private static final String GET_RECOMMENDED_FILMS_QUERY = """
            SELECT
                f.id AS id,
                f.name AS name,
                f.description AS description,
                f.release_date AS release_date,
                f.duration AS duration,
                m.id AS mpa_id,
                m.name AS mpa_name,
                g.id AS genre_id,
                g.name AS genre_name,
                fl.user_id AS user_id,
                d.id AS director_id,
                d.name AS director_name
            FROM films f
            LEFT JOIN film_genres fg ON f.id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            LEFT JOIN film_director fd ON f.id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.id
            LEFT JOIN mpa m ON f.mpa_id = m.id
            WHERE f.id IN (
            SELECT film_id FROM film_likes
            WHERE user_id IN (
            SELECT fl1.user_id FROM film_likes fl1
            RIGHT JOIN film_likes fl2 ON fl2.film_id = fl1.film_id
            GROUP BY fl1.user_id, fl2.user_id
            HAVING fl1.user_id IS NOT NULL AND
            fl1.user_id != ? AND fl2.user_id = ?
            ORDER BY COUNT(fl1.user_id) DESC
            LIMIT ?)
            AND film_id NOT IN (
            SELECT film_id FROM film_likes
            WHERE user_id = ?))""";

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
        insertDirectors(film);
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
        insertDirectors(film);
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
        delete(DELETE_DIRECTOR, id);
        log.info("Удален фильм с ID {}", id);
    }

   @Override
    public Collection<FilmDto> getPopularFilms(int count, Long genre, Integer year) {
        Map<Long, FilmDto> filmMap = new LinkedHashMap<>();
        String sqlQuery;
        Object[] params;
        if (genre == null && year == null) {
            sqlQuery = FIND_POPULAR_WITHOUT_GENRE_YEAR;
            params = new Object[]{count};
        } else if (genre != null && year == null) {
            sqlQuery = FIND_POPULAR_WITH_GENRE;
            params = new Object[]{genre, count};
        } else if (genre == null) {
            sqlQuery = FIND_POPULAR_WITH_YEAR;
            params = new Object[]{year, count};
        } else {
            sqlQuery = FIND_POPULAR_WITH_GENRE_YEAR;
            params = new Object[]{genre, year, count};
        }

        jdbc.query(sqlQuery, rs -> {
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
                            .directors(new HashSet<>())
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

            Long directorId = rs.getObject("director_id", Long.class);
            if (directorId != null && directorId != 0) {
                film.getDirectors().add(Director.builder()
                        .id(directorId)
                        .name(rs.getString("director_name"))
                        .build());
            }
        }, params);

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

    @Override
    public Collection<Film> getSortedFilm(Long directorId, String sort) {
        String orderByClause = buildOrderByClause(sort);
        String sql = FIND_BY_DIRECTOR_ID;
        if (orderByClause != null) {
            sql += " ORDER BY " + orderByClause;
        }
        return findMany(sql, directorId);
    }

    @Override
    public Collection<Film> searchFilms(String query, List<String> by) {
        String likeQuery = "%" + query.toLowerCase() + "%";
        List<Object> params = new ArrayList<>();

        List<String> conditions = new ArrayList<>();
        if (by.contains("title")) {
            conditions.add("LOWER(f.name) LIKE ?");
            params.add(likeQuery);
        }
        if (by.contains("director")) {
            conditions.add("LOWER(d.name) LIKE ?");
            params.add(likeQuery);
        }

        String conditionsClause = conditions.isEmpty() ? "" : "OR " + String.join(" OR ", conditions);
        String sql = String.format(SEARCH_FILMS, conditionsClause);

        log.debug("Поиск SQL: {}", sql);
        log.debug("Параметры поиска: {}", params);

        Collection<Film> results = findMany(sql, params.toArray());
        log.debug("Результаты поиска: {}", results);

        return results;
    }

    private String buildOrderByClause(String sort) {
        if (sort == null || sort.isEmpty()) {
            return null;
        }

        String[] sortParams = sort.split(",");
        List<String> orderBy = new ArrayList<>();

        for (String param : sortParams) {
            switch (param.trim().toLowerCase()) {
                case "year":
                    orderBy.add("f.release_date ASC");
                    break;
                case "likes":
                    orderBy.add("COUNT(fl.user_id) DESC");
                    break;
                default:
                    break;
            }
        }
        return orderBy.isEmpty() ? null : String.join(", ", orderBy);
    }

    private void updateGenres(Film film) {
        update(DELETE_GENRES, film.getId());
        insertGenres(film);
    }

    private void insertDirectors(Film film) {
        if (film.getDirectors() == null) {
            return;
        }
        List<Object[]> batch = film.getDirectors().stream()
                .map(director -> new Object[]{film.getId(), director.getId()})
                .collect(Collectors.toList());

        jdbc.batchUpdate(INSERT_DIRECTOR, batch, batch.size(), (ps, args) -> {
            ps.setLong(1, (Long) args[0]);
            ps.setLong(2, (Long) args[1]);
        });

        batch.forEach(args -> log.info("Добавлен режиссер {} к фильму: {}", args[1], args[0]));
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

    @Override
    public Collection<FilmDto> getRecommendedFilms(long id, int limit) {
        Map<Long, FilmDto> filmMap = new LinkedHashMap<>();

        jdbc.query(GET_RECOMMENDED_FILMS_QUERY, (rs) -> {
            long filmId = rs.getLong("id");
            FilmDto film = filmMap.computeIfAbsent(filmId, k -> {
                try {
                    return FilmDto.builder()
                            .id(rs.getLong("id"))
                            .name(rs.getString("name"))
                            .description(rs.getString("description"))
                            .releaseDate(rs.getDate("release_date").toLocalDate())
                            .duration(rs.getInt("duration"))
                            .mpa(Mpa.builder().build())
                            .genres(new HashSet<>())
                            .likes(new HashSet<>())
                            .likesCount(0)
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
            film.setLikesCount(film.getLikes().size());
            Long directorId = rs.getObject("director_id", Long.class);
            if (directorId != null && directorId != 0) {
                film.getDirectors().add(Director.builder()
                        .id(directorId)
                        .name(rs.getString("director_name"))
                        .build());
            }
        }, id, id, limit, id);
        return filmMap.values();
    }

}
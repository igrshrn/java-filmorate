package ru.yandex.practicum.filmorate.dal.film;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class FilmResultSetExtractor implements ResultSetExtractor<Map<Long, Film>> {
    private final FilmRowMapper mapper;

    public FilmResultSetExtractor(FilmRowMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Map<Long, Film> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, Film> filmMap = new HashMap<>();

        while (rs.next()) {
            long filmId = rs.getLong("film_id");
            Film film = filmMap.computeIfAbsent(filmId, k -> {
                try {
                    return mapper.mapRow(rs, 1);
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

        }

        return filmMap;
    }
}

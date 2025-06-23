package ru.yandex.practicum.filmorate.dal.film;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class FilmDtoResultSetExtractor implements ResultSetExtractor<Map<Long, FilmDto>> {
    private final FilmDtoRowMapper mapper;

    public FilmDtoResultSetExtractor(FilmDtoRowMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Map<Long, FilmDto> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, FilmDto> filmMap = new LinkedHashMap<>();
        while (rs.next()) {
            long filmId = rs.getLong("id");
            FilmDto film = filmMap.computeIfAbsent(filmId, k -> {
                try {
                    return mapper.mapRow(rs, 1);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            film.setMpa(Mpa.builder().id(rs.getLong("mpa_id")).name(rs.getString("mpa_name")).build());
            Long genreId = rs.getObject("genre_id", Long.class);
            if (genreId != null && genreId != 0) {
                film.getGenres().add(Genre.builder().id(genreId).name(rs.getString("genre_name")).build());
            }
            Long userId = rs.getObject("user_id", Long.class);
            if (userId != null && userId != 0) {
                film.getLikes().add(userId);
            }
            film.setLikesCount(film.getLikes().size());
            Long directorId = rs.getObject("director_id", Long.class);
            if (directorId != null && directorId != 0) {
                film.getDirectors().add(Director.builder().id(directorId).name(rs.getString("director_name")).build());
            }
        }
        return filmMap;
    }
}

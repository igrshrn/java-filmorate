package ru.yandex.practicum.filmorate.dal.film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@Component
public class FilmRowMapper implements RowMapper<Film>, Serializable {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {

        return Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("film_name"))
                .description(rs.getString("film_description"))
                .releaseDate(rs.getDate("film_release_date").toLocalDate())
                .duration(rs.getInt("film_duration"))
                .mpa(Mpa.builder().build())
                .genres(new HashSet<>())
                .likes(new HashSet<>())
                .build();
    }
}

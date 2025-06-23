package ru.yandex.practicum.filmorate.dal.film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@Component
public class FilmDtoRowMapper implements RowMapper<FilmDto>, Serializable {

    @Override
    public FilmDto mapRow(ResultSet rs, int rowNum) throws SQLException {
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
                .directors(new HashSet<>())
                .build();
    }
}

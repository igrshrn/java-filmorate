package ru.yandex.practicum.filmorate.dal.genre;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class GenreResultSetExtractor implements ResultSetExtractor<Map<Long, Genre>> {
    @Override
    public Map<Long, Genre> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, Genre> genreMap = new HashMap<>();
        while (rs.next()) {
            long id = rs.getLong("id");
            String name = rs.getString("name");
            genreMap.computeIfAbsent(id, k -> Genre.builder()
                    .id(id)
                    .name(name)
                    .build());
        }
        return genreMap;
    }
}

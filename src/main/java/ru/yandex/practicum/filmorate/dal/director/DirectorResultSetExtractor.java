package ru.yandex.practicum.filmorate.dal.director;


import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class DirectorResultSetExtractor implements ResultSetExtractor<Map<Long, Director>> {
    @Override
    public Map<Long, Director> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, Director> directorMap = new HashMap<>();

        while (rs.next()) {
            long id = rs.getLong("id");
            String name = rs.getString("name");
            directorMap.computeIfAbsent(id, k -> Director.builder()
                    .id(id)
                    .name(name)
                    .build());
        }
        return directorMap;
    }
}
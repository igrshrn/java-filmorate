package ru.yandex.practicum.filmorate.dal.mpa;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class MpaResultSetExtractor implements ResultSetExtractor<Map<Long, Mpa>> {
    @Override
    public Map<Long, Mpa> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, Mpa> mpaMap = new HashMap();
        while (rs.next()) {
            long id = rs.getLong("id");
            String name = rs.getString("name");
            mpaMap.computeIfAbsent(id, k -> Mpa.builder()
                    .id(id)
                    .name(name)
                    .build());
        }
        return mpaMap;
    }
}

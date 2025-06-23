package ru.yandex.practicum.filmorate.dal.event;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class EventResultSetExtractor implements ResultSetExtractor<Map<Long, Event>> {
    private final EventRowMapper mapper;

    public EventResultSetExtractor(EventRowMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Map<Long, Event> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, Event> eventMap = new LinkedHashMap<>();

        while (rs.next()) {
            long eventId = rs.getLong("event_id");
            Event event = eventMap.computeIfAbsent(eventId, k -> {
                try {
                    return mapper.mapRow(rs, 1);
                } catch (SQLException e) {
                    throw new RuntimeException("Ошибка маппинга", e);
                }
            });
        }
        return eventMap;
    }
}
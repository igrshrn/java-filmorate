package ru.yandex.practicum.filmorate.dal.event;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<Event>, Serializable {

    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(rs.getLong("event_id"))
                .timestamp(rs.getTimestamp("timestamp").getTime())
                .userId(rs.getLong("user_id"))
                .eventType(Event.EventType.valueOf(rs.getString("event_type").toUpperCase()))
                .operation(Event.Operation.valueOf(rs.getString("operation").toUpperCase()))
                .entityId(rs.getLong("entity_id"))
                .build();
    }
}
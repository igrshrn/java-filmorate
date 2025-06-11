package ru.yandex.practicum.filmorate.storage.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EventDbStorage implements EventStorage  {
    private final JdbcTemplate jdbcTemplate;

    public EventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addEvent(Event event) {
        String sql = "INSERT INTO events (user_id, event_type, operation, entity_id) VALUES (?,?,?,?)";
        jdbcTemplate.update(sql, event.getUserId(), event.getEventType().name(), event.getOperation().name(), event.getEntityId());
    }

    @Override
    public List<Event> getFeed(long userId) {
        String sql = "SELECT * FROM events WHERE user_id =? ORDER BY timestamp ASC";
        return jdbcTemplate.query(sql, this::mapRowToEvent, userId);
    }

    private Event mapRowToEvent(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(rs.getLong("event_id"))
                .timestamp(rs.getTimestamp("timestamp").getTime())
                .userId(rs.getLong("user_id"))
                .eventType(Event.EventType.valueOf(rs.getString("event_type")))
                .operation(Event.Operation.valueOf(rs.getString("operation")))
                .entityId(rs.getLong("entity_id"))
                .build();
    }
}

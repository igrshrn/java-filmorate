package ru.yandex.practicum.filmorate.storage.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.event.EventResultSetExtractor;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;

@Slf4j
@Repository
public class EventDbStorage extends BaseRepository<Event> implements EventStorage {
    private static final String INSERT_EVENT = "INSERT INTO events (user_id, event_type, operation, entity_id) VALUES (?,?,?,?)";
    private static final String SELECT_FEED = "SELECT * FROM events WHERE user_id = ? ORDER BY timestamp ASC";
    private static final String DELETE_FEED = "DELETE FROM events WHERE user_id = ?";

    public EventDbStorage(JdbcTemplate jdbc, EventResultSetExtractor extractor) {
        super(jdbc, extractor);
        log.info("EventResultSetExtractor инициализирован: {}", extractor != null);
    }

    @Override
    public void addEvent(Event event) {
        update(INSERT_EVENT, event.getUserId(), event.getEventType().name(),
                event.getOperation().name(), event.getEntityId());
    }

    @Override
    public List<Event> getFeed(long userId) {
        return findMany(SELECT_FEED, userId);
    }

    @Override
    public void deleteFeed(long userId) {
        delete(DELETE_FEED, userId);
    }
}

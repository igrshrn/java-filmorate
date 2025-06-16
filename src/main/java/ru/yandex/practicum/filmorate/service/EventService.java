package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
public class EventService {
    private final EventStorage eventStorage;

    public EventService(EventStorage eventStorage) {
        this.eventStorage = eventStorage;
    }

    public void addEvent(long userId, Event.EventType eventType, Event.Operation operation, long entityId) {
        Event event = Event.builder()
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .build();
        log.info("Добавлено событие {} ", event);
        eventStorage.addEvent(event);
    }

    public List<Event> getFeed(long userId) {
        return eventStorage.getFeed(userId);
    }

    public void deleteFeed(long userId) {
        log.info("Удалено событие событие {} ", userId);
        eventStorage.deleteFeed(userId);
    }

}

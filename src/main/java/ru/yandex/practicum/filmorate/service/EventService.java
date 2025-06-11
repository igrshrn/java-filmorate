package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.util.List;

@Service
public class EventService {
    private final EventStorage eventStorage;

    public EventService(EventStorage eventStorage) {
        this.eventStorage = eventStorage;
    }

    public void addEvent(Event event) {
        eventStorage.addEvent(event);
    }

    public List<Event> getFeed(long userId) {
        return eventStorage.getFeed(userId);
    }
}

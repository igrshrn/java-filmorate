package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    private long eventId;
    private long timeStamp;
    private long userId;
    private EventType eventType;
    private Operation operation;
    private long entityId;

    public enum EventType {
        LIKE, REVIEW, FRIEND
    }

    public enum Operation {
        REMOVE, ADD, UPDATE
    }
}

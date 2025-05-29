package ru.practicum.evmsevice.model;

import java.util.Optional;

public enum EventState {
    PENDING,
    PUBLISHED,
    CANCELED;

    public static Optional<EventState> from(String state) {
        for (EventState eventState : EventState.values()) {
            if (eventState.name().equalsIgnoreCase(state)) {
                return Optional.of(eventState);
            }
        }
        return Optional.empty();
    }
}

package ru.practicum.evmsevice.mapper;

import ru.practicum.evmsevice.dto.NewEventDto;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.model.EventState;

import java.time.LocalDateTime;

public class EventMapper {
    private EventMapper() {}

    public static Event mapNewEvent(final NewEventDto newDto) {
        Event event = new Event();
        event.setAnnotation(newDto.getAnnotation());
        event.setDescription(newDto.getDescription());
        event.setEventDate(newDto.getEventDate());
        event.setCreatedOn(LocalDateTime.now());
        event.setLat(newDto.getLocation().getLat());
        event.setLon(newDto.getLocation().getLon());
        event.setPaid(newDto.getPaid());
        event.setParticipantLimit(newDto.getParticipantLimit());
        event.setRequestModeration(newDto.getRequestModeration());
        event.setState(EventState.PENDING.name());
        return event;
    }
}

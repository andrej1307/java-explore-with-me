package ru.practicum.evmsevice.mapper;

import ru.practicum.evmsevice.dto.EventFullDto;
import ru.practicum.evmsevice.dto.EventShortDto;
import ru.practicum.evmsevice.dto.NewEventDto;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.enums.EventState;
import ru.practicum.evmsevice.model.Location;

import java.time.LocalDateTime;

public class EventMapper {
    private EventMapper() {}

    public static Event toEvent(final NewEventDto newDto) {
        Event event = new Event();
        event.setAnnotation(newDto.getAnnotation());
        event.setDescription(newDto.getDescription());
        event.setEventDate(newDto.getEventDate());
        event.setCreatedOn(LocalDateTime.now());
        event.setLat(newDto.getLocation().getLat());
        event.setLon(newDto.getLocation().getLon());
        event.setPaid(false);
        if(newDto.getPaid() != null) {
            event.setPaid(newDto.getPaid());
        }
        event.setParticipantLimit(0);
        if(newDto.getParticipantLimit() != null) {
            event.setParticipantLimit(newDto.getParticipantLimit());
        }
        event.setRequestModeration(false);
        if (newDto.getRequestModeration() != null) {
            event.setRequestModeration(newDto.getRequestModeration());
        }
        event.setState(EventState.PENDING);
        event.setTitle(newDto.getTitle());
        return event;
    }

    public static EventFullDto toFullDto(Event event) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setCreatedOn(event.getCreatedOn());
        dto.setCategory(CategoryMapper.toDto(event.getCategory()));
        dto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        dto.setLocation(new Location(event.getLat(), event.getLon()));
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setConfirmedRequests(0);
        if(event.getConfirmedRequests() != null) {
            dto.setConfirmedRequests(event.getConfirmedRequests());
        }
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState());
        dto.setPaid(event.getPaid());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setTitle(event.getTitle());
        return dto;
    }

    public static EventShortDto toShortDto(Event event) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(CategoryMapper.toDto(event.getCategory()));
        dto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        dto.setEventDate(event.getEventDate());
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        return dto;
    }
}

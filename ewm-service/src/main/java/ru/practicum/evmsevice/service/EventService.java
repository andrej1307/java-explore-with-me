package ru.practicum.evmsevice.service;

import ru.practicum.evmsevice.dto.EventFullDto;
import ru.practicum.evmsevice.dto.EventShortDto;
import ru.practicum.evmsevice.dto.NewEventDto;

import java.util.List;

public interface EventService {
    EventFullDto createEvent(NewEventDto newEventDto, Integer userId);

    EventFullDto getEventById(Integer eventId, Integer userId);

    List<EventShortDto> getEventsByUserId(Integer userId);

    EventFullDto patchEvent(Integer eventId, EventShortDto eventShortDto, Integer userId);
}

package ru.practicum.evmsevice.service;

import ru.practicum.evmsevice.dto.*;
import ru.practicum.evmsevice.model.Event;

import java.util.List;

public interface EventService {
    EventFullDto createEvent(NewEventDto newEventDto, Integer userId);

    EventFullDto getEventById(Integer eventId, Integer userId);

    List<EventShortDto> getEventsByUserId(Integer userId, Integer from, Integer size);

    EventFullDto patchEvent(Integer eventId, UpdateEventUserRequest eventDto, Integer userId);

    EventFullDto adminUpdateEvent(Integer eventId, UpdateEventAdminRequest eventDto);

    Event findEventById(Integer eventId);

    List<EventShortDto> findEventsByParametrs(String text,
                                              List<Integer> categories,
                                              Boolean paid,
                                              String rangeStart,
                                              String rangeEnd,
                                              Boolean onlyAvailable,
                                              String sort,
                                              Integer from, Integer size);
}

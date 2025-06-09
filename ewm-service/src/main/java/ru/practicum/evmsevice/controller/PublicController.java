package ru.practicum.evmsevice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmsevice.client.StatsClient;
import ru.practicum.evmsevice.dto.EventFullDto;
import ru.practicum.evmsevice.dto.EventShortDto;
import ru.practicum.evmsevice.enums.EventState;
import ru.practicum.evmsevice.exception.NotFoundException;
import ru.practicum.evmsevice.mapper.EventMapper;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.service.EventService;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.evmsevice.mapper.EventMapper.toFullDto;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping()
public class PublicController {
    @Value("${spring.application.name}")
    private String appName;
    private final StatsClient statsClient;
    private final EventService eventService;

    @GetMapping("/events/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto findEventById(@PathVariable("id") int id,
                                      HttpServletRequest request) {
        log.info("Пользователь запрвшмвает для просмотра событие: {}", id);
        Event event = eventService.findEventById(id);
        // Событие должно быть опубликовано
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Среди опубликованных не найдено событие id=" + id);
        }
        // сохраняем запрос в сервере статистики
        statsClient.hitInfo(appName, request);
        EventFullDto eventFullDto = EventMapper.toFullDto(event);
        eventFullDto.setViews(statsClient.getEventViews(id, true));
        return eventFullDto;
    }

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> findAllEvents(
                                            @RequestParam(name = "text", required = false) String text,
                                            @RequestParam(name = "categories", required = false) List<Integer> categories,
                                            @RequestParam(name = "paid", required = false) Boolean paid,
                                            @RequestParam(name = "rangeStart", required = false) String rangeStart,
                                            @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
                                            @RequestParam(name = "onlyAvailable", defaultValue = "true") Boolean onlyAvailable,
                                            @RequestParam(name = "sort", defaultValue = "EVENT_DATE") String sort,
                                            @RequestParam(name = "from", defaultValue = "0") Integer from,
                                            @RequestParam(name = "size", defaultValue = "10") Integer size,
                                            HttpServletRequest request) {
        log.info("Пользователь запрвшмвает поиск событий.");
        List<EventShortDto> eventDtos = eventService.findEventsByParametrs(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size
        );
        return eventDtos;
    }
}

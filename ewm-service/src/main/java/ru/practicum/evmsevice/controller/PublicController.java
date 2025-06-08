package ru.practicum.evmsevice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmsevice.client.StatsClient;
import ru.practicum.evmsevice.dto.EventFullDto;
import ru.practicum.evmsevice.enums.EventState;
import ru.practicum.evmsevice.exception.NotFoundException;
import ru.practicum.evmsevice.mapper.EventMapper;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.service.EventService;

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
        log.info("Пользователь просматривает событие: {}", id);
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
}

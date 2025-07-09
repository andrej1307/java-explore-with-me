package ru.practicum.evmsevice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmsevice.dto.EventFullDto;
import ru.practicum.evmsevice.dto.UpdateEventAdminRequest;
import ru.practicum.evmsevice.service.EventService;

import java.util.List;

/**
 * Класс обработки запросов администратора
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/events")
public class AdminEventsController {
    private final EventService eventService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> findEvents(
            @RequestParam(name = "users", required = false) List<Integer> users,
            @RequestParam(name = "states", required = false) List<String> states,
            @RequestParam(name = "categories", required = false) List<Integer> categories,
            @RequestParam(name = "rangeStart", required = false) String rangeStart,
            @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Администратор запрашивает список событий. users:{}, states:{}, categories:{} rangeStart:{},  rangeStart:{}.",
                users, states, categories, rangeStart, rangeEnd);
        return eventService.findEventsByAdmin(states, users, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable Integer eventId,
                                    @RequestBody @Validated UpdateEventAdminRequest eventDto) {
        log.info("Администратор редактирует событие id={}. {}", eventId, eventDto);
        return eventService.adminUpdateEvent(eventId, eventDto);
    }
}

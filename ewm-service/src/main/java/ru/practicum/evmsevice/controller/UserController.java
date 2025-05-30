package ru.practicum.evmsevice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmsevice.dto.EventFullDto;
import ru.practicum.evmsevice.dto.NewEventDto;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.service.EventService;
import ru.practicum.evmsevice.service.UserService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")public class UserController {
    private final EventService eventService;

    @PostMapping("/{id}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable int id,
                                    @Validated @RequestBody NewEventDto eventDto) {
        log.info("Creating new event {}", eventDto);
        return eventService.createEvent(eventDto, id);
    }
}

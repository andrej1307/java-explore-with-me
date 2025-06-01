package ru.practicum.evmsevice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmsevice.dto.*;
import ru.practicum.evmsevice.mapper.RequestMapper;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.model.Request;
import ru.practicum.evmsevice.service.EventService;
import ru.practicum.evmsevice.service.RequestService;
import ru.practicum.evmsevice.service.UserService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")public class UserController {
    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping("/{id}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable int id,
                                    @Validated @RequestBody NewEventDto eventDto) {
        log.info("Пользователь id={} cоздает новое событие: {}", id, eventDto.getTitle());
        return eventService.createEvent(eventDto, id);
    }

    @GetMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable int userId,
                                 @PathVariable int eventId) {

        log.info("Пользователь id={} запрашивает информацию о событии id={}. ",
                userId, eventId);
        return eventService.getEventById(eventId, userId);
    }

    @GetMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvents(@PathVariable int userId,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Пользователь id={} запрашивает информацию об инциированных событиях.", userId);
        return eventService.getEventsByUserId(userId, from, size);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable Integer userId,
                                    @PathVariable Integer eventId,
                                    @RequestBody UpdateEventUserRequest eventDto) {
        log.info("Пользователь id={} изменяет информацию об инциированном событии id={}.",
                userId, eventId);
        return eventService.patchEvent(eventId, eventDto, userId);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto createRequet(@PathVariable Integer userId,
                                   @RequestParam(name = "eventId", required = true) Integer eventId) {
        log.info("Пользователь id={} создает запрос на участие в событии id={}.",
                userId, eventId);
        Request request = requestService.createRequest(userId, eventId);
        return RequestMapper.toRequestDto(request);
    }

    @GetMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<RequestDto> findRequestsByUserId(@PathVariable Integer userId) {
        log.info("Пользователь id={} выполняет поиск собственных запросов.", userId);
        return requestService.getRequestsByUserId(userId)
                .stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    @DeleteMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public RequestDto deleteRequestById( @PathVariable Integer userId,
                                         @PathVariable Integer requestId) {
        log.info("Пользователь id={} удаляет запрос id={}.", userId, requestId);
        return RequestMapper.toRequestDto(requestService.deleteRequest(userId, requestId));
    }
}

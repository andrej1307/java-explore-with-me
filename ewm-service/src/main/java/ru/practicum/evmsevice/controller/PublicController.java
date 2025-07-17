package ru.practicum.evmsevice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmsevice.client.StatsClient;
import ru.practicum.evmsevice.dto.*;
import ru.practicum.evmsevice.enums.EventState;
import ru.practicum.evmsevice.exception.NotFoundException;
import ru.practicum.evmsevice.mapper.CategoryMapper;
import ru.practicum.evmsevice.mapper.EventMapper;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.service.CategoryService;
import ru.practicum.evmsevice.service.CommentService;
import ru.practicum.evmsevice.service.CompilationService;
import ru.practicum.evmsevice.service.EventService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping()
public class PublicController {
    private final StatsClient statsClient;
    private final EventService eventService;
    private final CompilationService compilationService;
    private final CategoryService categoryService;
    private final CommentService commentService;
    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/events/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto findEventById(@PathVariable("id") int id,
                                      HttpServletRequest request) {
        log.info("Пользователь запрашивает для просмотра событие: {}", id);
        Event event = eventService.findEventById(id);
        // Событие должно быть опубликовано
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Среди опубликованных не найдено событие id=" + id);
        }
        // сохраняем запрос в сервере статистики
        statsClient.hitInfo(appName, request.getRequestURI(), request.getRemoteAddr());
        return EventMapper.toFullDto(event);
    }

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> findAllEvents(
            @RequestParam(name = "text", required = false) String text,
            @RequestParam(name = "categories", required = false) List<Integer> categories,
            @RequestParam(name = "paid", required = false) Boolean paid,
            @RequestParam(name = "rangeStart", required = false)
            String rangeStart,
            @RequestParam(name = "rangeEnd", required = false)
            String rangeEnd,
            @RequestParam(name = "onlyAvailable", defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(name = "sort", defaultValue = "EVENT_DATE") String sort,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            HttpServletRequest request) {
        log.info("Пользователь запрашивает поиск событий: содержащих текст:{}, categories:{}, rangeStart:{}, rangeEnd:{}.",
                text, categories, rangeStart, rangeEnd);
        List<EventShortDto> eventDtos = eventService.findEventsByParametrs(text, categories,
                paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        for (EventShortDto eventDto : eventDtos) {
            statsClient.hitInfo(appName, String.format("/events/%d", eventDto.getId()), request.getRemoteAddr());
        }
        return eventDtos;
    }

    @GetMapping("/compilations")
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> findAllCompilations(
            @RequestParam(name = "pinned", required = false) Boolean pinned,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Пользователь запрашивает список подборок.");
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto findCompilationById(@PathVariable("compId") int compId) {
        log.info("Пользователь запрашивает подборку id={}.", compId);
        return compilationService.getCompilation(compId);
    }

    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> findCategories(@RequestParam(name = "from", defaultValue = "0") Integer from,
                                            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Пользователь запрашивает список категорий.");
        return categoryService.getAllCategories().stream()
                .map(CategoryMapper::toDto)
                .skip(from).limit(size)
                .toList();
    }

    @GetMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto findCategoryById(@PathVariable("catId") int catId) {
        log.info("Пользователь запрашивает категорию id={}.", catId);
        return CategoryMapper.toDto(categoryService.getCategoryById(catId));
    }

    @GetMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getCommentsByEventId(@PathVariable Integer eventId,
                                                 @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                 @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Поиск всех коментариев к событию id={}.", eventId);
        return commentService.getCommentsByEventId(eventId, from, size);
    }

    @GetMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto getComment(@PathVariable Integer commentId) {
        log.info("Поиск коментария id={}.", commentId);
        return commentService.getCommentById(commentId);
    }

}

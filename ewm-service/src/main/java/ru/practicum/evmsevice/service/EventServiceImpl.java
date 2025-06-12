package ru.practicum.evmsevice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.evmsevice.client.StatsClient;
import ru.practicum.evmsevice.dto.*;
import ru.practicum.evmsevice.enums.EventAdminAction;
import ru.practicum.evmsevice.enums.EventState;
import ru.practicum.evmsevice.enums.EventUserAction;
import ru.practicum.evmsevice.exception.NotFoundException;
import ru.practicum.evmsevice.exception.ValidationException;
import ru.practicum.evmsevice.mapper.EventMapper;
import ru.practicum.evmsevice.model.Category;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.model.EventConfirmedRequestCount;
import ru.practicum.evmsevice.model.User;
import ru.practicum.evmsevice.repository.EventRepository;
import ru.practicum.evmsevice.repository.EventSpecification;
import ru.practicum.evmsevice.repository.RequestRepository;
import ru.practicum.statdto.StatsDto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class EventServiceImpl implements EventService{
    private static final DateTimeFormatter DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Integer HOURS_EVENT_DELAY = 2;

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final StatsClient statsClient;

    /**
     * Создание нового события
     * @param newEventDto - новое событие
     * @param userId - иденитификатор пользователя инициатора
     * @return - сохраненный объект информации о событии
     */
    @Override
    public EventFullDto createEvent(NewEventDto newEventDto, Integer userId) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(HOURS_EVENT_DELAY))) {
            throw new ValidationException(
                    "Field: eventDate. Error: не может быть раньше, чем через "
                            + HOURS_EVENT_DELAY + " часа от текущего момента. Value: "
                            + newEventDto.getEventDate().format(DATA_TIME_FORMATTER)
            );
        }
        User user = userService.getUserById(userId);
        Category category = categoryService.getCategoryById(newEventDto.getCategory());
        Event event = EventMapper.toEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        Event savedEvent = eventRepository.save(event);
        return EventMapper.toFullDto(savedEvent);
    }

    @Override
    public EventFullDto getEventById(Integer eventId, Integer userId) {
        Event event = findEventById(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Пользователь id=" + userId
                    + " не является инициатором события id=" + eventId);
        }
        EventFullDto eventFullDto = EventMapper.toFullDto(event);
        eventFullDto.setConfirmedRequests(requestRepository.getCountConfirmedRequestsByEventId(eventId));
        eventFullDto.setViews(statsClient.getEventViews(eventId, true));
        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Integer userId, Integer from, Integer size) {
        List<Event> events = eventRepository.findEventsByInitiator_Id(userId);
        return events.stream()
                .skip(from)
                .limit(size)
                .map(EventMapper::toShortDto).toList();
    }

    @Override
    public EventFullDto patchEvent(Integer eventId, UpdateEventUserRequest eventDto, Integer userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException("Не найдено событие id=" + eventId));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Пользователь id=" + userId
                    + " не является инициатором события id=" + eventId);
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(HOURS_EVENT_DELAY))) {
            throw new ValidationException(
                    "Field: eventDate. Error: не может быть раньше, чем через "
                            + HOURS_EVENT_DELAY + " часа от текущего момента. Value: "
                            + event.getEventDate().format(DATA_TIME_FORMATTER)
            );
        }
        if (eventDto.getAnnotation() != null) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(eventDto.getCategory()));
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            if (eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(HOURS_EVENT_DELAY))) {
                throw new ValidationException(
                        "Field: eventDate. Error: новое значение не может быть раньше, чем через "
                                + HOURS_EVENT_DELAY + " часа от текущего момента. Value: "
                                + eventDto.getEventDate().format(DATA_TIME_FORMATTER)
                );
            }
            event.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getLocation() != null) {
            event.setLat(eventDto.getLocation().getLat());
            event.setLon(eventDto.getLocation().getLon());
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(EventUserAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            } else if (eventDto.getStateAction().equals(EventUserAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            }
        }
        if (eventDto.getTitle() != null) {
            event.setTitle(eventDto.getTitle());
        }
        Event savedEvent = eventRepository.save(event);
        return EventMapper.toFullDto(savedEvent);
    }

    @Override
    public EventFullDto adminUpdateEvent(Integer eventId, UpdateEventAdminRequest eventDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException("Не найдено событие id=" + eventId));
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(HOURS_EVENT_DELAY))) {
            throw new ValidationException(
                    "Field: eventDate. Error: не может быть раньше, чем через "
                            + HOURS_EVENT_DELAY + " часа от текущего момента. Value: "
                            + event.getEventDate().format(DATA_TIME_FORMATTER)
            );
        }
        if (eventDto.getAnnotation() != null) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(eventDto.getCategory()));
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            if (eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(HOURS_EVENT_DELAY))) {
                throw new ValidationException(
                        "Field: eventDate. Error: новое значение не может быть раньше, чем через "
                                + HOURS_EVENT_DELAY + " часа от текущего момента. Value: "
                                + eventDto.getEventDate().format(DATA_TIME_FORMATTER)
                );
            }
            event.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getLocation() != null) {
            event.setLat(eventDto.getLocation().getLat());
            event.setLon(eventDto.getLocation().getLon());
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(EventAdminAction.PUBLISH_EVENT)) {
                if (!event.getState().equals(EventState.PENDING)) {
                    throw new ValidationException(
                            "Field: stateAction. Error: " +
                                    "Событие id=" + eventId + " должно быть в состоянии ожидания публикации." +
                                    " Value: " + eventDto.getStateAction()
                    );
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (eventDto.getStateAction().equals(EventAdminAction.REJECT_EVENT)) {
                if(event.getState().equals(EventState.PUBLISHED)) {
                   throw new ValidationException(
                           "Field: stateAction. Error: " +
                                   "Нельзя удалить опубликованное событие id=" + eventId +
                                   " Value: " + eventDto.getStateAction()
                   );
                }
                event.setState(EventState.REJECTED);
            }
        }
        if (eventDto.getTitle() != null) {
            event.setTitle(eventDto.getTitle());
        }
        Event savedEvent = eventRepository.save(event);
        return EventMapper.toFullDto(savedEvent);
    }

    @Override
    public Event findEventById(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException("Не найдено событие id=" + eventId));
        return event;
    }

    /**
     * поиск событий пользователем
     */
    @Override
    public List<EventShortDto> findEventsByParametrs(String text,
                                                     List<Integer> categories,
                                                     Boolean paid,
                                                     String rangeStart,
                                                     String rangeEnd,
                                                     Boolean onlyAvailable,
                                                     String sort,
                                                     Integer from,
                                                     Integer size) {


        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        try {
            if (rangeStart != null && !rangeStart.isEmpty()) {
                startDate = LocalDateTime.parse(rangeStart, DATA_TIME_FORMATTER);
            }
            if (rangeEnd != null && !rangeEnd.isEmpty()) {
                endDate = LocalDateTime.parse(rangeEnd, DATA_TIME_FORMATTER);
            }
            // если в запросе не указан диапазон дат [rangeStart-rangeEnd],
            // то нужно выгружать события, которые произойдут позже текущей даты и времени
            if (startDate != null && endDate != null) {
                startDate = LocalDateTime.now();
            }
        } catch (DateTimeParseException e) {
            throw new ValidationException("Некорректный формат времени. " + e.getMessage());
        }

        Specification<Event> spec = Specification.where(null);
        // Задаем спецификации для поиска событий
        // ...поиск событий по тексту в аннотации и подробном описании события
        if (text != null) {
            spec = spec.and(EventSpecification.annotetionContains(text));
            spec = spec.or(EventSpecification.descriptionContains(text));
        }
        // ... поиск по списку идентификаторов категорй
        if (categories != null) {
            spec = spec.and(EventSpecification.categoryIn(categories));
        }
        // ... поиск платных или бесплатных событий
        if (paid != null) {
            spec = spec.and(EventSpecification.paidEqual(paid));
        }
        // Поиск по date события
        if (startDate != null) {
            spec = spec.and(EventSpecification.eventDateAfter(startDate));
        }
        if (endDate != null) {
            spec = spec.and(EventSpecification.eventDateBefore(endDate));
        }

        List<Event> events = eventRepository.findAll(spec,
                Sort.by("eventDate").descending());

        TreeMap<Integer,EventShortDto> eventMap = new TreeMap<Integer,EventShortDto>();
        List<String> eventUris = new ArrayList<>();
        for (Event event : events) {
            eventMap.put(event.getId(), EventMapper.toShortDto(event));
            eventUris.add(String.format("/events/%d", event.getId()));
        }

        // заполняем количество заявок
        List<EventConfirmedRequestCount> counts =
                requestRepository.getCountConfirmedRequests(eventMap.keySet().stream().toList());
        for(EventConfirmedRequestCount count : counts) {
            Integer eventId = count.getEventId();
            eventMap.get(eventId).setConfirmedRequest(count.getConfirmedRequestCount().intValue());
        }

        // заполняем количество просмотров
        List<StatsDto> statsDtos = statsClient.getEventViewsByUris(eventUris, true);
        for (StatsDto dto : statsDtos) {
            Integer eventId = Integer.parseInt(dto.getUri().split("/")[2]);
            eventMap.get(eventId).setViews(dto.getHits());
        }

        List<EventShortDto> eventDtos = new ArrayList<>();
        if(onlyAvailable) {
            // Фильтруем события у котрыз не исчерпано количество заявок
            eventDtos = eventMap.values()
                    .stream()
                    .filter(eventDto -> eventDto.getParticipantLimit() != 0
                            && eventDto.getConfirmedRequest() < eventDto.getParticipantLimit())
                    .toList();
        } else {
            eventDtos.addAll(eventMap.values());
        }

        if(sort.equalsIgnoreCase("VIEWS")) {
            return eventDtos.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews))
                    .skip(from).limit(size).toList();
        }
        return eventMap.values().stream().skip(from).limit(size).toList();
    }

    /**
     * Поиск событий администратором
     */
    @Override
    public List<EventFullDto> findEventsByAdmin(List<String> states,
                                                List<Integer> users,
                                                List<Integer> categories,
                                                String rangeStart,
                                                String rangeEnd,
                                                Integer from,
                                                Integer size) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        try {
            if (rangeStart != null && !rangeStart.isEmpty()) {
                startDate = LocalDateTime.parse(rangeStart, DATA_TIME_FORMATTER);
            }
            if (rangeEnd != null && !rangeEnd.isEmpty()) {
                endDate = LocalDateTime.parse(rangeEnd, DATA_TIME_FORMATTER);
            }
            if (startDate != null && endDate != null) {
                startDate = LocalDateTime.now();
            }
        } catch (DateTimeParseException e) {
            throw new ValidationException("Некорректный формат времени. " + e.getMessage());
        }

        Specification<Event> spec = Specification.where(null);
        // Задаем спецификации для поиска событий
        // ...поиск событий по списку идентификаторов инициаторов
        if (users != null) {
            spec = spec.and(EventSpecification.eventInitiatorIdIn(users));
        }
        // ... поиск по списку идентификаторов категорй
        if (categories != null) {
            spec = spec.and(EventSpecification.categoryIn(categories));
        }
        // ... поиск по списку состояний
        // List<EventState> enumStates = states.stream().map(state -> EventState.valueOf(state)).toList();
        if (states != null) {
            spec = spec.and(EventSpecification.eventStateIn(states));
        }
        // Поиск по date события
        if (startDate != null) {
            spec = spec.and(EventSpecification.eventDateAfter(startDate));
        }
        if (endDate != null) {
            spec = spec.and(EventSpecification.eventDateBefore(endDate));
        }

        List<Event> events = eventRepository.findAll(spec,
                Sort.by("eventDate").descending());
        if (events.isEmpty()) {
            return List.of();
        }

        TreeMap<Integer,EventFullDto> eventMap = new TreeMap<Integer,EventFullDto>();
        List<String> eventUris = new ArrayList<>();
        for (Event event : events) {
            eventMap.put(event.getId(), EventMapper.toFullDto(event));
            eventUris.add(String.format("/events/%d", event.getId()));
        }

        // заполняем количество заявок
        List<EventConfirmedRequestCount> counts =
                requestRepository.getCountConfirmedRequests(eventMap.keySet().stream().toList());
        for(EventConfirmedRequestCount count : counts) {
            Integer eventId = count.getEventId();
            eventMap.get(eventId).setConfirmedRequests(count.getConfirmedRequestCount().intValue());
        }

        // заполняем количество просмотров
        List<StatsDto> statsDtos = statsClient.getEventViewsByUris(eventUris, true);
        for (StatsDto dto : statsDtos) {
            Integer eventId = Integer.parseInt(dto.getUri().split("/")[2]);
            eventMap.get(eventId).setViews(dto.getHits());
        }

        List<EventFullDto> eventDtos = new ArrayList<>();
        eventDtos.addAll(eventMap.values());
        return eventDtos.stream().skip(from).limit(size).toList();
    }
}

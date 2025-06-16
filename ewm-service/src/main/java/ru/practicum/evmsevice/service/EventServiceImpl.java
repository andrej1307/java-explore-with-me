package ru.practicum.evmsevice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.evmsevice.client.StatsClient;
import ru.practicum.evmsevice.dto.*;
import ru.practicum.evmsevice.enums.EventAdminAction;
import ru.practicum.evmsevice.enums.EventState;
import ru.practicum.evmsevice.enums.EventUserAction;
import ru.practicum.evmsevice.exception.BadRequestException;
import ru.practicum.evmsevice.exception.DataConflictException;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

@Service
@Transactional
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final DateTimeFormatter DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Integer HOURS_EVENT_DELAY = 2;

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final StatsClient statsClient;

    /**
     * Создание нового события
     *
     * @param newEventDto - новое событие
     * @param userId      - иденитификатор пользователя инициатора
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

    /**
     * Получение подробной информации о событии инициатором
     *
     * @param eventId - идентификатор события
     * @param userId  - идентификатор инициатора
     * @return - объект события
     */
    @Override
    public EventFullDto getEventById(Integer eventId, Integer userId) {
        Event event = findEventById(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Пользователь id=" + userId
                    + " не является инициатором события id=" + eventId);
        }
        event.setConfirmedRequests(requestRepository.getCountConfirmedRequestsByEventId(eventId));
        event.setViews(statsClient.getEventViews(eventId, true));
        return EventMapper.toFullDto(event);
    }

    /**
     * Поиск событий по идентификатору инициатора
     *
     * @param userId - идентификатор инициатора
     * @param from   - с какого события отображать список
     * @param size   - размер списка
     * @return - список событий
     */
    @Override
    public List<EventShortDto> getEventsByUserId(Integer userId, Integer from, Integer size) {
        List<Event> events = eventRepository.findEventsByInitiator_Id(userId);
        updateViwesAndRequests(events);
        return events.stream()
                .skip(from)
                .limit(size)
                .map(EventMapper::toShortDto)
                .toList();
    }

    /**
     * Заполняем объекты списка событий сведениями о просмотрах и подтвержденных заявках
     *
     * @param events - список событий
     */
    private void updateViwesAndRequests(List<Event> events) {
        TreeMap<Integer, Event> eventMap = new TreeMap<Integer, Event>();
        List<String> eventUris = new ArrayList<>();
        for (Event event : events) {
            eventMap.put(event.getId(), event);
            eventUris.add(String.format("/events/%d", event.getId()));
        }
        // заполняем количество заявок
        List<EventConfirmedRequestCount> counts =
                requestRepository.getCountConfirmedRequests(eventMap.keySet().stream().toList());
        for (EventConfirmedRequestCount count : counts) {
            Integer eventId = count.getEventId();
            eventMap.get(eventId).setConfirmedRequests(count.getConfirmedRequestCount().intValue());
        }
        // заполняем количество просмотров
        List<StatsDto> statsDtos = statsClient.getEventViewsByUris(eventUris, true);
        for (StatsDto dto : statsDtos) {
            Integer eventId = Integer.parseInt(dto.getUri().split("/")[2]);
            eventMap.get(eventId).setViews(dto.getHits());
        }
    }

    /**
     * Обновление события инициатором
     *
     * @param eventId  - идентификатор события
     * @param eventDto - объект с обновляемыми данными
     * @param userId   - идентификатор инициатора
     * @return - обновленный объект события
     */
    @Override
    public EventFullDto patchEvent(Integer eventId, @Validated UpdateEventUserRequest eventDto, Integer userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException("Не найдено событие id=" + eventId));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new DataConflictException("Пользователь id=" + userId
                    + " не является инициатором события id=" + eventId);
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(HOURS_EVENT_DELAY))) {
            throw new ValidationException(
                    "Field: eventDate. Error: не может быть раньше, чем через "
                            + HOURS_EVENT_DELAY + " часа от текущего момента. Value: "
                            + event.getEventDate().format(DATA_TIME_FORMATTER)
            );
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException(
                    "Field: event.state. Error: Недопустимый статус события для изменения."
                            + " Value: " + event.getState());
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
        savedEvent.setConfirmedRequests(requestRepository.getCountConfirmedRequestsByEventId(eventId));
        savedEvent.setViews(statsClient.getEventViews(eventId, true));
        return EventMapper.toFullDto(savedEvent);
    }

    /**
     * Обновление события администратором
     *
     * @param eventId  - идентификатор события
     * @param eventDto - объект с обновляемыми данными
     * @return - обновленный объект события
     */
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
                    throw new DataConflictException(
                            "Field: stateAction. Error: " +
                                    "Событие id=" + eventId + " должно быть в состоянии ожидания публикации." +
                                    " Value: " + eventDto.getStateAction()
                    );
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (eventDto.getStateAction().equals(EventAdminAction.REJECT_EVENT)) {
                if (event.getState().equals(EventState.PUBLISHED)) {
                    throw new DataConflictException(
                            "Field: stateAction. Error: " +
                                    "Нельзя удалить опубликованное событие id=" + eventId +
                                    " Value: " + eventDto.getStateAction()
                    );
                }
                event.setState(EventState.REJECTED);
            } else {
                throw new ValidationException(
                        "Field: stateAction. Error: " +
                                "Указано непредусмотренное действие. " +
                                " Value: " + eventDto.getStateAction());
            }
        }
        if (eventDto.getTitle() != null) {
            event.setTitle(eventDto.getTitle());
        }
        Event savedEvent = eventRepository.save(event);
        savedEvent.setConfirmedRequests(requestRepository.getCountConfirmedRequestsByEventId(eventId));
        savedEvent.setViews(statsClient.getEventViews(eventId, true));
        return EventMapper.toFullDto(savedEvent);
    }

    @Override
    public Event findEventById(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException("Не найдено событие id=" + eventId));
        event.setConfirmedRequests(requestRepository.getCountConfirmedRequestsByEventId(eventId));
        event.setViews(statsClient.getEventViews(eventId, true));
        return event;
    }

    /**
     * поиск событий пользователем
     */
    @Override
    public List<EventShortDto> findEventsByParametrs(String text, List<Integer> categories,
                                                     Boolean paid, String rangeStart, String rangeEnd,
                                                     Boolean onlyAvailable, String sort, Integer from, Integer size) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (rangeStart != null && !rangeStart.isEmpty()) {
            try {
                startDate = LocalDateTime.parse(rangeStart, DATA_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new ValidationException("Некорректный формат времени. " + e.getMessage());
            }
        }
        if (rangeEnd != null && !rangeEnd.isEmpty()) {
            try {
                endDate = LocalDateTime.parse(rangeEnd, DATA_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new ValidationException("Некорректный формат времени. " + e.getMessage());
            }
        }
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new BadRequestException(
                        "Parametr: rangeStart, rangeEnd. " +
                                "Error: Введен некорректный интервал времени." +
                                ". Value: " + startDate.format(DATA_TIME_FORMATTER) +
                                ", " + endDate.format(DATA_TIME_FORMATTER)
                );
            }
        }
        // если в запросе не указан диапазон дат [rangeStart-rangeEnd],
        // то нужно выгружать события, которые произойдут позже текущей даты и времени
        if (startDate == null && endDate == null) {
            startDate = LocalDateTime.now();
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
                Sort.by("eventDate"));
        if (events.isEmpty()) {
            return List.of();
        }
        updateViwesAndRequests(events);
        List<EventShortDto> eventDtos;
        if (onlyAvailable) {
            // Фильтруем события у котрых не исчерпано количество заявок
            eventDtos = events.stream()
                    .filter(event -> event.getParticipantLimit() != 0
                            && event.getConfirmedRequests() < event.getParticipantLimit())
                    .map(EventMapper::toShortDto)
                    .toList();
        } else {
            eventDtos = events.stream().map(EventMapper::toShortDto).toList();
        }

        if (sort.equalsIgnoreCase("VIEWS")) {
            return eventDtos.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews).reversed())
                    .skip(from).limit(size).toList();
        }
        return eventDtos.stream().skip(from).limit(size).toList();
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
                Sort.by("eventDate"));
        if (events.isEmpty()) {
            return List.of();
        }
        updateViwesAndRequests(events);
        List<EventFullDto> eventDtos = events.stream()
                .map(EventMapper::toFullDto)
                .toList();
        return eventDtos.stream().skip(from).limit(size).toList();
    }

    /**
     * Поиск событий по списку идентификаторов
     */
    @Override
    public List<Event> findEventsByIdIn(List<Integer> eventIds) {
        List<Event> events = eventRepository.findEventsByIdIn(eventIds);
        if (events.isEmpty()) {
            return List.of();
        }
        updateViwesAndRequests(events);
        return events;
    }

}

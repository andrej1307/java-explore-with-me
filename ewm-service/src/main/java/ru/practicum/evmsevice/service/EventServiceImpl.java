package ru.practicum.evmsevice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
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
import ru.practicum.evmsevice.model.User;
import ru.practicum.evmsevice.repository.EventRepository;
import ru.practicum.evmsevice.repository.EventSpecification;
import ru.practicum.evmsevice.repository.RequestRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EventServiceImpl implements EventService{
    private static final DateTimeFormatter DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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
        event.setConfirmedRequests(requestRepository.getCountConfirmedRequestsByEventId(eventId));
        EventFullDto eventFullDto = EventMapper.toFullDto(event);
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
        event.setConfirmedRequests(requestRepository.getCountConfirmedRequestsByEventId(eventId));
        return event;
    }

    /**
     * поиск событий
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
        Specification<Event> spec = Specification.where(null);

        if (text != null) {
            spec = spec.and(EventSpecification.annotetionContains(text));
            spec = spec.or(EventSpecification.descriptionContains(text));
        }
        if (categories != null) {
            spec = spec.and(EventSpecification.categoryIn(categories));
        }

        List<Event> events = eventRepository.findAll(spec);
        return events.stream().map(EventMapper::toShortDto).toList();
    }
}

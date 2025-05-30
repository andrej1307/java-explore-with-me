package ru.practicum.evmsevice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.evmsevice.dto.EventFullDto;
import ru.practicum.evmsevice.dto.EventShortDto;
import ru.practicum.evmsevice.dto.NewEventDto;
import ru.practicum.evmsevice.mapper.EventMapper;
import ru.practicum.evmsevice.model.Category;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.model.User;
import ru.practicum.evmsevice.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EventServiceImpl implements EventService{
    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    @Override
    public EventFullDto createEvent(NewEventDto newEventDto, Integer userId) {
        User user = userService.getUserById(userId);
        Category category = categoryService.getCategoryById(newEventDto.getCategory());
        Event event = EventMapper.toEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setPublishedOn(LocalDateTime.now());
        Event savedEvent = eventRepository.save(event);
        return EventMapper.toFullDto(savedEvent);
    }

    @Override
    public EventFullDto getEventById(Integer eventId, Integer userId) {
        return null;
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Integer userId) {
        return List.of();
    }

    @Override
    public EventFullDto patchEvent(Integer eventId, EventShortDto eventShortDto, Integer userId) {
        return null;
    }
}

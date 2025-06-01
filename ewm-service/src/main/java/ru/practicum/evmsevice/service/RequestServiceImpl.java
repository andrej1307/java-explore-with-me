package ru.practicum.evmsevice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.evmsevice.enums.EventState;
import ru.practicum.evmsevice.enums.RequestStatus;
import ru.practicum.evmsevice.exception.NotFoundException;
import ru.practicum.evmsevice.exception.ValidationException;
import ru.practicum.evmsevice.model.*;
import ru.practicum.evmsevice.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventService eventService;
    private final UserService userService;

    @Override
    public Request createRequest(Integer userId, Integer eventId) {
        Event event = eventService.findEventById(eventId);
        if (event.getInitiator().getId().equals(userId)) {
            throw new ValidationException(
                    "Field: event.initiator_id. Error: " +
                    "Инициатор события не может добавить запрос на участие в своём событии. " +
                    "Value: " + event.getInitiator().getId()
            );
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException(
                    "Field: event.state. Error: " +
                    "Нельзя участвовать в неопубликованном событии. " +
                    "Value: " + event.getState()
            );
        }
        if (event.getConfirmedRequests().equals(event.getParticipantLimit())) {
            throw new ValidationException(
                    "Field: event.state. Error: " +
                    "У события достигнут лимит запросов на участие. " +
                    "Value: " + event.getConfirmedRequests()
            );
        }
        Request request = new Request();
        User user = userService.getUserById(userId);
        request.setRequester(user);
        request.setEvent(event);
        if (event.getRequestModeration()) {
            request.setStatus(RequestStatus.PENDING);
        } else {
            request.setStatus(RequestStatus.APPROVED);
        }
        request.setCreated(LocalDateTime.now());
        Request savedRequest = requestRepository.save(request);
        return savedRequest;
    }

    @Override
    public List<Request> getRequestsByUserId(Integer userId) {
        List<Request> requests = requestRepository.findAllByRequester_Id(userId);
        return requests;
    }

    @Override
    public Request deleteRequest(Integer userId, Integer requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new NotFoundException("Не найден запрос id=" + requestId));
        if (!request.getRequester().getId().equals(userId)) {
            throw new ValidationException(
                    "Field: request.requestor.id. Error: " +
                            "Нельзя удалить чужой запрос. " +
                            "Value: " + request.getRequester().getId()
            );
        }
        requestRepository.delete(request);
        return request;
    }
}

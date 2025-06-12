package ru.practicum.evmsevice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.evmsevice.dto.RequestGroupDto;
import ru.practicum.evmsevice.dto.RequestUpdateDto;
import ru.practicum.evmsevice.enums.EventState;
import ru.practicum.evmsevice.enums.RequestStatus;
import ru.practicum.evmsevice.exception.DataConflictException;
import ru.practicum.evmsevice.exception.NotFoundException;
import ru.practicum.evmsevice.exception.ValidationException;
import ru.practicum.evmsevice.mapper.RequestMapper;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.model.Request;
import ru.practicum.evmsevice.model.User;
import ru.practicum.evmsevice.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.Collections;
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
        Integer confirmedRequests = requestRepository.getCountConfirmedRequestsByEventId(eventId);
        if (confirmedRequests != null) {
            if (confirmedRequests.equals(event.getParticipantLimit())) {
                throw new ValidationException(
                        "Field: event.state. Error: " +
                                "У события достигнут лимит запросов на участие. " +
                                "Value: " + confirmedRequests
                );
            }
        }
        Request request = new Request();
        User user = userService.getUserById(userId);
        request.setRequester(user);
        request.setEvent(event);
        request.setStatus(RequestStatus.PENDING);
        if (!event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        request.setCreated(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Override
    public List<Request> getRequestsByUserId(Integer userId) {
        return requestRepository.findAllByRequester_Id(userId);
    }

    @Override
    public Request deleteRequest(Integer userId, Integer requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new NotFoundException("Не найден запрос id=" + requestId));
        if (!request.getRequester().getId().equals(userId)) {
            throw new ValidationException(
                    "Field: request.requester.id. Error: " +
                            "Нельзя удалить чужой запрос. " +
                            "Value: " + request.getRequester().getId()
            );
        }
        requestRepository.delete(request);
        return request;
    }

    /**
     * Метод изменения поиска запросов к событию
     */
    @Override
    public List<Request> getRequestsByEventId(Integer userId, Integer eventId) {
        Event event = eventService.findEventById(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException(
                    "Field: event.initiator_id. " +
                            "Error: пользователь id=" + userId + " не является инициатором события id=" + eventId +
                            ". Value: " + event.getInitiator().getId()
            );
        }
        return requestRepository.findAllByEvent_Id(eventId);
    }

    /**
     * Метод изменения статуса запросов
     */
    @Override
    public RequestGroupDto updateRequestsStatus(Integer userId, Integer eventId, RequestUpdateDto requestUpdateDto) {
        Event event = eventService.findEventById(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException(
                    "Field: event.initiator_id. Error: " +
                            "Пользователь id=" + userId + " не является инициатором события id=" + eventId +
                            ". Value: " + event.getInitiator().getId()
            );
        }

        // ...нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие
        // (Ожидается код ошибки 409)
        Integer confirmedRequests = requestRepository.getCountConfirmedRequestsByEventId(eventId);
        if ((event.getParticipantLimit() > 0)
                && event.getParticipantLimit().equals(confirmedRequests)) {
            throw new DataConflictException(
                    "Field: event.confirmedRequests. " +
                            "Error: Достигнуто максимальное количество заявок для события id=" + eventId +
                            ". Value: " + confirmedRequests
            );
        }

        RequestGroupDto requestGroupDto = new RequestGroupDto();
        List<Integer> requestIds = requestUpdateDto.getRequestIds();
        if (requestIds.isEmpty()) {
            return requestGroupDto;
        }
        Collections.sort(requestIds);
        RequestStatus status = requestUpdateDto.getStatus();

        // Проверяем заявки из списка
        for (Integer requestId : requestIds) {
            Request request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("Не найдена заявка id=" + requestId));
            // ... статус можно изменить только у заявок, находящихся в состоянии ожидания
            // (Ожидается код ошибки 409)
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new DataConflictException(
                        "Field: request.status. " +
                                "Error: недопустимый статус заявки id=" + requestId +
                                ". Value: " + request.getStatus()
                );
            }
            // ... если при подтверждении данной заявки, лимит заявок для события исчерпан,
            // то все неподтверждённые заявки необходимо отклонить
            if (confirmedRequests.equals(event.getParticipantLimit())) {
                status = RequestStatus.REJECTED;
            }
            request.setStatus(status);
            Request savedRequest = requestRepository.save(request);
            if (savedRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
                requestGroupDto.getConfirmedRequests().add(RequestMapper.toRequestDto(savedRequest));
                confirmedRequests++;
            } else if (savedRequest.getStatus().equals(RequestStatus.REJECTED)) {
                requestGroupDto.getRejectedRequests().add(RequestMapper.toRequestDto(savedRequest));
            }
        }
        return requestGroupDto;
    }
}

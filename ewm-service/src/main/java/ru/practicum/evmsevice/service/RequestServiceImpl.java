package ru.practicum.evmsevice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.evmsevice.dto.RequestGroupDto;
import ru.practicum.evmsevice.dto.RequestUpdateDto;
import ru.practicum.evmsevice.enums.EventState;
import ru.practicum.evmsevice.enums.RequestStatus;
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
        Integer confirmedRequests = event.getConfirmedRequests();
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
            event.setConfirmedRequests(confirmedRequests + 1);
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

        if (requestUpdateDto.getRequestIds().isEmpty()) {
            throw new ValidationException(
                    "Field: requestIds.size. " +
                            "Error: список идентификаторов запроса пустой. " +
                            "Value: 0."
            );
        }

        if (event.getRequestModeration()
                || ((event.getParticipantLimit() != null) && (event.getParticipantLimit() > 0))) {
            if (event.getParticipantLimit().equals(event.getConfirmedRequests())) {
                throw new ValidationException(
                        "Field: event.confirmedRequests. " +
                                "Error: Достигнуто максимальное количество заявок для события id=" + eventId +
                                ". Value: " + event.getInitiator().getId()
                );
            }
        }
        RequestGroupDto requestGroupDto = new RequestGroupDto();
        RequestStatus status = requestUpdateDto.getStatus();
        List<Integer> requestIds = requestUpdateDto.getRequestIds();
        System.out.printf("==%s\n", requestIds.toString());

        // Если запросы откланены меняем без проверок статус всех запросов
        if (status == RequestStatus.REJECTED) {
            requestRepository.updateStatus(status, requestIds);
            requestGroupDto.setRejectedRequests(
                    requestRepository.findAllByIdIsIn(requestIds)
                            .stream()
                            .map(RequestMapper::toRequestDto)
                            .toList()
            );
            return requestGroupDto;
        }

        Integer participantLimit = 0;
        if (event.getParticipantLimit() != null) {
            participantLimit = event.getParticipantLimit();
        }
        // Если лимит участников не установлен, то меняем статус у всех сразу
        if (participantLimit == 0) {
            requestRepository.updateStatus(status, requestIds);
            requestGroupDto.setConfirmedRequests(
                    requestRepository.findAllByIdIsIn(requestIds)
                            .stream()
                            .map(RequestMapper::toRequestDto)
                            .toList()
            );
            return requestGroupDto;
        }

        Integer ConfirmedRequests = 0;
        if(event.getConfirmedRequests() != null) {
            ConfirmedRequests = event.getConfirmedRequests();
        }
        Collections.sort(requestIds);
        for (int i = 0; i < requestIds.size(); i++) {
            System.out.printf("* i=%d\n", i);
            Integer requestId = requestIds.get(i);
            Request request = requestRepository.findById(requestId)
                    .orElseThrow(() ->
                            new NotFoundException("Не найден запрос id=" + requestId));
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ValidationException(
                        "Field: requestIds.status. " +
                                "Error: Невозможно изменить статус запроса id=" + request.getId() +
                                "Value: " + request.getStatus()
                );
            }
            if (!event.getConfirmedRequests().equals(event.getParticipantLimit())) {
                request.setStatus(status);
                requestRepository.save(request);
                requestGroupDto.getConfirmedRequests().add(RequestMapper.toRequestDto(request));
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            } else {
                status = RequestStatus.REJECTED;
                request.setStatus(status);
                requestRepository.save(request);
                requestGroupDto.getRejectedRequests().add(RequestMapper.toRequestDto(request));
            }
        }
        return requestGroupDto;
    }
}

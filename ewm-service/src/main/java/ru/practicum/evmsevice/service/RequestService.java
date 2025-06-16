package ru.practicum.evmsevice.service;

import ru.practicum.evmsevice.dto.RequestDto;
import ru.practicum.evmsevice.dto.RequestGroupDto;
import ru.practicum.evmsevice.dto.RequestUpdateDto;
import ru.practicum.evmsevice.model.Request;

import java.util.List;

public interface RequestService {
    Request createRequest(Integer userId, Integer eventId);

    List<RequestDto> getRequestsByUserId(Integer userId);

    Request CanceledRequest(Integer userId, Integer requestId);

    List<Request> getRequestsByEventId(Integer userId, Integer eventId);

    RequestGroupDto updateRequestsStatus(Integer userId, Integer eventId, RequestUpdateDto requestUpdateDto);
}

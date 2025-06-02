package ru.practicum.evmsevice.service;

import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.model.Request;

import java.util.List;

public interface RequestService {
    Request createRequest(Integer userId, Integer eventId );

    List<Request> getRequestsByUserId(Integer userId);

    Request deleteRequest(Integer userId, Integer requestId);

    List<Request> getRequestsByEventId(Integer userId, Integer eventId);

}

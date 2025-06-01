package ru.practicum.evmsevice.mapper;

import ru.practicum.evmsevice.dto.RequestDto;
import ru.practicum.evmsevice.model.Request;

public class RequestMapper {
    private RequestMapper() {}
    public static RequestDto toRequestDto(Request request) {
        RequestDto requestDto = new RequestDto();
        requestDto.setId(request.getId());
        requestDto.setCreated(request.getCreated());
        requestDto.setEvent(request.getEvent().getId());
        requestDto.setRequester(request.getRequester().getId());
        return requestDto;
    }
}

package ru.practicum.evmsevice.dto;

import lombok.*;
import ru.practicum.evmsevice.enums.RequestStatus;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestDto {
    private Integer id;
    private LocalDateTime created;
    private Integer event;
    private Integer requester;
    private RequestStatus status;
}

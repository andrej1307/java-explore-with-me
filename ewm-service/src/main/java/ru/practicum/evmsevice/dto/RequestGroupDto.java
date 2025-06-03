package ru.practicum.evmsevice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class RequestGroupDto {
    List<RequestDto> confirmedRequests;
    List<RequestDto> rejectedRequests;
}

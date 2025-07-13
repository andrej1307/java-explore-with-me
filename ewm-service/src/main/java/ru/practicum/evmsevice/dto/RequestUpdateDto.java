package ru.practicum.evmsevice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.evmsevice.enums.RequestStatus;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class RequestUpdateDto {
    List<Integer> requestIds = new ArrayList<>();
    RequestStatus status;
}

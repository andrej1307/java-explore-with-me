package ru.practicum.evmsevice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.evmsevice.enums.EventState;
import ru.practicum.evmsevice.enums.EventUserAction;

@Setter
@Getter
@NoArgsConstructor
public class UpdateEventUserRequest extends NewEventDto{
    private EventUserAction stateAction;
}

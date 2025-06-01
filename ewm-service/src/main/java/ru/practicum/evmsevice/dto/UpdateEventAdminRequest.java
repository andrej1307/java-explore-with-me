package ru.practicum.evmsevice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.evmsevice.enums.EventAdminAction;

@Setter
@Getter
@NoArgsConstructor
public class UpdateEventAdminRequest extends NewEventDto{
    private EventAdminAction stateAction;
}

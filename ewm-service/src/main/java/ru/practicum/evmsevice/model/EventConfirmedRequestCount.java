package ru.practicum.evmsevice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EventConfirmedRequestCount {
    private Integer eventId;
    private Long confirmedRequestCount;
}

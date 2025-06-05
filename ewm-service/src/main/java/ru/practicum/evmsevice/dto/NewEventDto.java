package ru.practicum.evmsevice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.evmsevice.model.Location;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class NewEventDto
{
    @Size(min = 20, max = 2000, message = "длина аннотации 20 - 2000 символов.")
    private String annotation;
    @NotNull(message = "Категоря должна быть определена")
    private Integer category;
    @Size(min = 20, max = 7000, message = "длина описания 20 - 7000 символов.")
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @NotNull(message = "Место события должно быть определено.")
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    @Size(min = 3, max = 120, message = "длина заголовка 3 - 120 символов.")
    private String title;
}

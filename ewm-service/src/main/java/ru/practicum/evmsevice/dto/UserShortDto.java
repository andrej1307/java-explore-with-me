package ru.practicum.evmsevice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserShortDto {
    private Integer id;
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
}

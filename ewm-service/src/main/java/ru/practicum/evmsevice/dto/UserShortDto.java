package ru.practicum.evmsevice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {
    private Integer id;
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
}

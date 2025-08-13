package ru.practicum.evmsevice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentDto {
    @NotBlank(message = "Текст комментария должен быть задан.")
    @Size(min = 3, max = 2000, message = "Длина комментария должна быть от 3 до 2000 символов.")
    private String text;
}

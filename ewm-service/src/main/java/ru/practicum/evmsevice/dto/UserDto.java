package ru.practicum.evmsevice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {
    private Integer id;
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен удовлетворять правилам формирования почтовых адресов.")
    private String email;
}

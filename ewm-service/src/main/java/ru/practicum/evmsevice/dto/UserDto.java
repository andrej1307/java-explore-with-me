package ru.practicum.evmsevice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {
    private Integer id;
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 250, message = "длина имени должна быть 2 - 250 символов.")
    private String name;
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен удовлетворять правилам формирования почтовых адресов.")
    @Size(min = 6, max = 254, message = "длина имени должна быть 6 - 254 символов.")
    private String email;
}

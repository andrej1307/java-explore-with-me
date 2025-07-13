package ru.practicum.evmsevice.mapper;

import ru.practicum.evmsevice.dto.UserDto;
import ru.practicum.evmsevice.dto.UserShortDto;
import ru.practicum.evmsevice.model.User;

public class UserMapper {
    private UserMapper() {
    }

    public static User toUser(UserDto dto) {
        User user = new User();
        if (dto.getId() != null) {
            user.setId(dto.getId());
        }
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return user;
    }

    public static UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public static UserShortDto toUserShortDto(User user) {
        UserShortDto dto = new UserShortDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        return dto;
    }
}

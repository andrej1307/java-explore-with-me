package ru.practicum.evmsevice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmsevice.client.StatsClient;
import ru.practicum.evmsevice.dto.UserDto;
import ru.practicum.evmsevice.mapper.UserMapper;
import ru.practicum.evmsevice.model.User;
import ru.practicum.evmsevice.repository.UserRepository;
import ru.practicum.evmsevice.service.AdminUserService;
import ru.practicum.statdto.HitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Класс административных запросов по объектам "User"
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {
    @Value("${spring.application.name}")
    private String appName;
    private final StatsClient statsClient;
    private final AdminUserService adminUserService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers(HttpServletRequest request) {
        log.info("{} запрашивает список пользователей.", request.getRemoteUser());
        statsClient.hitInfo(appName, request);
        return adminUserService.getUsers().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser(HttpServletRequest request, @PathVariable Integer id) {
        log.info("Выполняем поиск пользователя id={}.", id);
        statsClient.hitInfo(appName, request);
        User user = adminUserService.getUserById(id);
        return UserMapper.toUserDto(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Validated @RequestBody UserDto userDto, HttpServletRequest request) {
        log.info("Создаем нового пользователя {}", userDto.toString());
        statsClient.hitInfo(appName, request);
        User savedUser =adminUserService.addUser(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(savedUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(HttpServletRequest request, @PathVariable Integer id) {
        log.info("Удаляем пользователя {}", id);
        statsClient.hitInfo(appName, request);
        adminUserService.deleteUser(id);
    }
}

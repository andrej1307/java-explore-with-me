package ru.practicum.evmsevice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmsevice.client.StatsClient;
import ru.practicum.evmsevice.dto.*;
import ru.practicum.evmsevice.mapper.CategoryMapper;
import ru.practicum.evmsevice.mapper.UserMapper;
import ru.practicum.evmsevice.model.Category;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.model.User;
import ru.practicum.evmsevice.service.CategoryService;
import ru.practicum.evmsevice.service.EventService;
import ru.practicum.evmsevice.service.UserService;

import java.util.List;

/**
 * Класс обработки запросов администратора
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Value("${spring.application.name}")
    private String appName;
    private final StatsClient statsClient;
    private final UserService userService;
    private final CategoryService categoryService;
    private final EventService eventService;

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers(HttpServletRequest request) {
        log.info("{} запрашивает список пользователей.", request.getRemoteUser());
        statsClient.hitInfo(appName, request);
        return userService.getUsers().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @GetMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser(HttpServletRequest request, @PathVariable Integer id) {
        log.info("Выполняем поиск пользователя id={}.", id);
        statsClient.hitInfo(appName, request);
        User user = userService.getUserById(id);
        return UserMapper.toUserDto(user);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Validated @RequestBody UserDto userDto, HttpServletRequest request) {
        log.info("Создаем нового пользователя {}", userDto.toString());
        statsClient.hitInfo(appName, request);
        User savedUser = userService.addUser(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(savedUser);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(HttpServletRequest request, @PathVariable Integer id) {
        log.info("Удаляем пользователя {}", id);
        statsClient.hitInfo(appName, request);
        userService.deleteUser(id);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Validated @RequestBody NewCategoryDto categoryDto,
                                      HttpServletRequest request) {
        log.info("Создаем категорию {}.", categoryDto.getName());
        statsClient.hitInfo(appName, request);
        Category newCategory = categoryService.createCategory(CategoryMapper.toCategory(categoryDto));
        return CategoryMapper.toDto(newCategory);
    }

    @PatchMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@Validated @RequestBody NewCategoryDto categoryDto,
                                      @PathVariable int id,
                                      HttpServletRequest request) {
        log.info("Обновляем категорию id={}.", id);
        statsClient.hitInfo(appName, request);
        Category category = CategoryMapper.toCategory(categoryDto);
        category.setId(id);
        Category updatedCategory = categoryService.updateCategory(category);
        return CategoryMapper.toDto(updatedCategory);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletecategory(@PathVariable int id, HttpServletRequest request) {
        log.info("Администратор удаляет категорию id={}.", id);
        statsClient.hitInfo(appName, request);
        categoryService.deleteCategory(id);
    }

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> findEvents(
            @RequestParam(name = "users", required = false) List<Integer> users,
            @RequestParam(name = "states", required = false) List<String> states,
            @RequestParam(name = "categories", required = false) List<Integer> categories,
            @RequestParam(name = "rangeStart", required = false) String rangeStart,
            @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            HttpServletRequest request) {
        log.info("Администратор запрашивает список событий. users:{}, states:{}, categories:{} rangeStart:{},  rangeStart:{}.",
                users, states, categories, rangeStart, rangeEnd);
        return eventService.findEventsByAdmin(states, users, categories, rangeStart, rangeEnd, from, size );
    }

    @PatchMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable Integer eventId,
                                    @RequestBody UpdateEventAdminRequest eventDto) {
        log.info("Администратор модерирует событие id={}.", eventId);
        return eventService.adminUpdateEvent(eventId, eventDto);
    }
}

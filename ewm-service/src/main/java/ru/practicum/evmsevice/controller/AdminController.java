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
import ru.practicum.evmsevice.model.User;
import ru.practicum.evmsevice.service.CategoryService;
import ru.practicum.evmsevice.service.CompilationService;
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
    private final StatsClient statsClient;
    private final UserService userService;
    private final CategoryService categoryService;
    private final EventService eventService;
    private final CompilationService compilationService;
    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers(
            @RequestParam(name = "ids", required = false) List<Integer> ids,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            HttpServletRequest request) {
        log.info("Администратор запрашивает  запрашивает список пользователей. {}", ids);
        statsClient.hitInfo(appName, request);
        List<User> users;
        if (ids != null) {
            users = userService.getUsers(ids);
        } else {
            users = userService.getUsers();
        }
        return users.stream()
                .map(UserMapper::toUserDto)
                .skip(from)
                .limit(size)
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
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
        return eventService.findEventsByAdmin(states, users, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable Integer eventId,
                                    @Validated @RequestBody UpdateEventAdminRequest eventDto) {
        log.info("Администратор редактирует событие id={}. {}", eventId, eventDto);
        return eventService.adminUpdateEvent(eventId, eventDto);
    }

    @PostMapping("/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Validated @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Администратор создает подборку событий \'{}\'.", newCompilationDto.getTitle());
        return compilationService.createCompilation(newCompilationDto);
    }

    @PatchMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable Integer compId,
                                            @Validated @RequestBody PatchCompilationDto compilationDto) {
        log.info("Администратор обновляет подборку событий \'{}\'.", compilationDto.getTitle());
        return compilationService.patchCompilation(compId, compilationDto);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Integer compId) {
        log.info("Администратор удаляет подборку событий id={}.", compId);
        compilationService.deleteCompilation(compId);
    }
}

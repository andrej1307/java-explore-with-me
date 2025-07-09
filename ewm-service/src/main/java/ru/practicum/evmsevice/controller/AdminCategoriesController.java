package ru.practicum.evmsevice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmsevice.dto.CategoryDto;
import ru.practicum.evmsevice.dto.NewCategoryDto;
import ru.practicum.evmsevice.service.CategoryService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/categories")
public class AdminCategoriesController {
    private final CategoryService categoryService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Validated @RequestBody NewCategoryDto categoryDto) {
        log.info("Создаем категорию {}.", categoryDto.getName());
        return categoryService.createCategory(categoryDto);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@Validated @RequestBody NewCategoryDto categoryDto,
                                      @PathVariable int id) {
        log.info("Обновляем категорию id={}.", id);
        return categoryService.updateCategory(id, categoryDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable int id) {
        log.info("Администратор удаляет категорию id={}.", id);
        categoryService.deleteCategory(id);
    }
}

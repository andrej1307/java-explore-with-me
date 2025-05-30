package ru.practicum.evmsevice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmsevice.client.StatsClient;
import ru.practicum.evmsevice.dto.CategoryDto;
import ru.practicum.evmsevice.dto.NewCategoryDto;
import ru.practicum.evmsevice.mapper.CategoryMapper;
import ru.practicum.evmsevice.model.Category;
import ru.practicum.evmsevice.service.CategoryService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    @Value("${spring.application.name}")
    private String appName;
    private final StatsClient statsClient;
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Validated @RequestBody NewCategoryDto categoryDto,
                                      HttpServletRequest request) {
        log.info("Создаем категорию {}.", categoryDto.getName());
        statsClient.hitInfo(appName, request);
        Category newCategory = categoryService.createCategory(CategoryMapper.toCategory(categoryDto));
        return CategoryMapper.toDto(newCategory);
    }

    @PatchMapping("/{id}")
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deletecategory(@PathVariable int id, HttpServletRequest request) {
        log.info("Удаляем категорию id={}.", id);
        statsClient.hitInfo(appName, request);
        categoryService.deleteCategory(id);
    }
}

package ru.practicum.evmsevice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmsevice.client.StatsClient;
import ru.practicum.evmsevice.model.Category;
import ru.practicum.evmsevice.service.AdminCategoryService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    @Value("${spring.application.name}")
    private String appName;
    private final StatsClient statsClient;
    private final AdminCategoryService adminCategoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category createCategory(@Validated @RequestBody Category category,
                                    HttpServletRequest request) {
        log.info("Создаем категорию {}.", category.getName());
        statsClient.hitInfo(appName, request);
        Category newCategory = adminCategoryService.createCategory(category);
        return newCategory;
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Category updateCategory(@Validated @RequestBody Category category,
                                   @PathVariable int id,
                                   HttpServletRequest request) {
        log.info("Обновляем категорию id={}.", id);
        statsClient.hitInfo(appName, request);
        category.setId(id);
        Category updatedCategory = adminCategoryService.updateCategory(category);
        return updatedCategory;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deletecategory(@PathVariable int id, HttpServletRequest request) {
        log.info("Удаляем категорию id={}.", id);
        statsClient.hitInfo(appName, request);
        adminCategoryService.deleteCategory(id);
    }
}

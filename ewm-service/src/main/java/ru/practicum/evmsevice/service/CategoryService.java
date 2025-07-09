package ru.practicum.evmsevice.service;

import ru.practicum.evmsevice.dto.CategoryDto;
import ru.practicum.evmsevice.dto.NewCategoryDto;
import ru.practicum.evmsevice.model.Category;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto categoryDto);

    CategoryDto updateCategory(Integer id, NewCategoryDto categoryDto);

    void deleteCategory(Integer id);

    Category getCategoryById(Integer id);

    List<Category> getAllCategories();
}

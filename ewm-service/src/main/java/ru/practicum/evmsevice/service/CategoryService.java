package ru.practicum.evmsevice.service;

import ru.practicum.evmsevice.model.Category;

public interface CategoryService {
    Category createCategory(Category category);
    Category updateCategory(Category category);
    void deleteCategory(Integer id);
    Category getCategoryById(Integer id);
}

package ru.practicum.evmsevice.service;

import ru.practicum.evmsevice.model.Category;

public interface AdminCategoryService {
    Category createCategory(Category category);
    Category updateCategory(Category category);
    void deleteCategory(Integer id);
}

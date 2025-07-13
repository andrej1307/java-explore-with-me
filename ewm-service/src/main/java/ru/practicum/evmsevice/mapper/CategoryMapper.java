package ru.practicum.evmsevice.mapper;

import ru.practicum.evmsevice.dto.CategoryDto;
import ru.practicum.evmsevice.dto.NewCategoryDto;
import ru.practicum.evmsevice.model.Category;

public class CategoryMapper {
    private CategoryMapper() {
    }

    public static Category toCategory(NewCategoryDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        return category;
    }

    public static CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}

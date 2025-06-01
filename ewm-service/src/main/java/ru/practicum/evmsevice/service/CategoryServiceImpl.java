package ru.practicum.evmsevice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.evmsevice.exception.NotFoundException;
import ru.practicum.evmsevice.model.Category;
import ru.practicum.evmsevice.repository.CategoryRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public Category createCategory(Category category) {
        Category savedCategory = categoryRepository.save(category);
        return savedCategory;
    }

    @Override
    public Category updateCategory(Category category) {
        categoryRepository.findById(category.getId())
                .orElseThrow(() -> new NotFoundException("Не найдена категория id=" + category.getId()));
        Category updatedCategory = categoryRepository.save(category);
        return updatedCategory;
    }

    @Override
    public void deleteCategory(Integer id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Не найдена категория id=" + id));
        categoryRepository.deleteById(id);
    }

    @Override
    public Category getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Не найдена категория id=" + id));
        return category;
    }
}

package ru.practicum.evmsevice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.evmsevice.dto.CategoryDto;
import ru.practicum.evmsevice.dto.NewCategoryDto;
import ru.practicum.evmsevice.exception.DataConflictException;
import ru.practicum.evmsevice.exception.NotFoundException;
import ru.practicum.evmsevice.mapper.CategoryMapper;
import ru.practicum.evmsevice.model.Category;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.repository.CategoryRepository;
import ru.practicum.evmsevice.repository.EventRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto categoryDto) {
        Category savedCategory = categoryRepository.save(CategoryMapper.toCategory(categoryDto));
        return CategoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Integer id, NewCategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Не найдена категория id=" + id));
        category.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        return CategoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Не найдена категория id=" + id));
        List<Event> events = eventRepository.findEventsByCategory_Id(id);
        if (events.size() > 0) {
            throw new DataConflictException(
                    "Категория id=" + id + " не пустая.");
        }
        categoryRepository.deleteById(id);
    }

    @Override

    public Category getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Не найдена категория id=" + id));
        return category;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}

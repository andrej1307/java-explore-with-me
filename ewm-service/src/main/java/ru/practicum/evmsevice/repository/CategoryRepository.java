package ru.practicum.evmsevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evmsevice.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}

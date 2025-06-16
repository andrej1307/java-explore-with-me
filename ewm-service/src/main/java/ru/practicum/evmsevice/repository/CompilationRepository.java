package ru.practicum.evmsevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evmsevice.model.Compilation;

import java.util.List;

public interface CompilationRepository  extends JpaRepository<Compilation, Integer> {
    List<Compilation> findAllByPinnedEquals(boolean pinned);
}

package ru.practicum.evmsevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evmsevice.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
}

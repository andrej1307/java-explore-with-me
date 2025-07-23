package ru.practicum.evmsevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.evmsevice.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer>,
        JpaSpecificationExecutor<Comment> {
    List<Comment> findAllByEventId(Integer eventId);

    List<Comment> findAllByAuthor_Id(Integer authorId);
}

package ru.practicum.evmsevice.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.evmsevice.enums.CommentState;
import ru.practicum.evmsevice.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public class CommentSpecification {
    public static Specification<Comment> commentEventIdEqual(Integer eventId) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("eventId")).value(eventId));
    }

    public static Specification<Comment> commentStateEqual(CommentState state) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("state")).value(state));
    }

    public static Specification<Comment> commentContains(String text) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("text"), "%" + text + "%"));
    }

    public static Specification<Comment> commentAuthorIdIn(List<Integer> userIds) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.join("author").get("id")).value(userIds));
    }

    public static Specification<Comment> commentCreatedAfter(LocalDateTime startDateTime) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdOn"), startDateTime));
    }

    public static Specification<Comment> commentCreatedBefore(LocalDateTime endDateTime) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("createdOn"), endDateTime));
    }

}

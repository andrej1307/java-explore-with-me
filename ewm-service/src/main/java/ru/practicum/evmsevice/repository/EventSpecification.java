package ru.practicum.evmsevice.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.evmsevice.model.Event;

import java.util.List;

public class EventSpecification {
    public static Specification<Event> annotetionContains(String text) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("annotation"), "%" + text + "%"));
    }

    public static Specification<Event> descriptionContains(String text) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("description"), "%" + text + "%"));
    }

    public static Specification<Event> categoryIn(List<Integer> categories) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.join("category").get("id")).value(categories));
    }
}

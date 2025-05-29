package ru.practicum.evmsevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evmsevice.model.Event;

public interface EventRepository extends JpaRepository<Event, Integer> {
}

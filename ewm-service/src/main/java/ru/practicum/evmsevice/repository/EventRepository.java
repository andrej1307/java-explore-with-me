package ru.practicum.evmsevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evmsevice.model.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findEventsByInitiator_Id(int id);
}

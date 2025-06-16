package ru.practicum.evmsevice.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.evmsevice.model.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer>,
                                            JpaSpecificationExecutor<Event> {
    List<Event> findEventsByInitiator_Id(int id);

    List<Event> findEventsByIdIn(List<Integer> ids);
}

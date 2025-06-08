package ru.practicum.evmsevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.evmsevice.enums.RequestStatus;
import ru.practicum.evmsevice.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request,Integer> {
    List<Request> findAllByRequester_Id(int userId);

    List<Request> findAllByEvent_Id(int eventId);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Integer getCountConfirmedRequestsByEventId(@Param("eventId") int eventId);
}

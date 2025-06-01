package ru.practicum.evmsevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evmsevice.enums.RequestStatus;
import ru.practicum.evmsevice.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request,Integer> {
    List<Request> findAllByRequester_Id(int userId);

    // Integer countByEvent_IdAAndStatusEquals(int eventId, RequestStatus status);
}

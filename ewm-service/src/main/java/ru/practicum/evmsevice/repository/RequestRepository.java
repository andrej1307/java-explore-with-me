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

    @Query("UPDATE Request r SET r.status = :status WHERE r.id IN :ids ")
    void updateStatus(@Param("status") RequestStatus status, @Param("ids") List<Integer> ids);

    List<Request> findAllByIdIsIn(List<Integer> ids);
}

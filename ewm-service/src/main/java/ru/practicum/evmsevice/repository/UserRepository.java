package ru.practicum.evmsevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evmsevice.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
}

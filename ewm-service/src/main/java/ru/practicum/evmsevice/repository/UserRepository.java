package ru.practicum.evmsevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evmsevice.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
}

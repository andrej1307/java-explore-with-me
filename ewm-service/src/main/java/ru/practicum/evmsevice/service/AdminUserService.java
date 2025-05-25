package ru.practicum.evmsevice.service;

import ru.practicum.evmsevice.model.User;

import java.util.List;

public interface AdminUserService {
    List<User> getUsers();

    User getUserById(Integer id);

    User addUser(User user);

    void deleteUser(Integer id);
}

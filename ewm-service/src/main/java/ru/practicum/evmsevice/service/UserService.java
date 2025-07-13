package ru.practicum.evmsevice.service;

import ru.practicum.evmsevice.model.User;

import java.util.List;

public interface UserService {
    List<User> getUsers();

    List<User> getUsers(List<Integer> ids);

    User getUserById(Integer id);

    User addUser(User user);

    void deleteUser(Integer id);
}

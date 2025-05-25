package ru.practicum.evmsevice.service;

import org.springframework.stereotype.Service;
import ru.practicum.evmsevice.exception.NotFoundException;
import ru.practicum.evmsevice.model.User;
import ru.practicum.evmsevice.repository.UserRepository;

import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {
    private final UserRepository userRepository;

    public AdminUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User addUser(User user) {
        User savedUser = userRepository.save(user);
        return savedUser;
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Не найден пользователь id=" + id));
        return user;
    }

    @Override
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Не найден пользователь id=" + id));
        userRepository.deleteById(id);
    }
}

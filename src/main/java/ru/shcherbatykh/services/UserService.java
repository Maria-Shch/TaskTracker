package ru.shcherbatykh.services;

import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.models.User;

import java.util.List;

public interface UserService {
    @Transactional
    List<User> users();

    @Transactional
    void addUser(User user);

    @Transactional
    User getUser(long id);

    @Transactional
    void deleteUser(long id);
}

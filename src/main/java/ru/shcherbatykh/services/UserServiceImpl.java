package ru.shcherbatykh.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.repositories.UserRepository;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override @Transactional
    public List<User> users() {
        return userRepository.findAll();
    }

    @Override @Transactional
    public void addUser(User user) {
        userRepository.save(user);
        //todo check non-repeatable login
    }

    @Override @Transactional
    public User getUser(long id) {
        return userRepository.getUserById(id);
    }

    @Override @Transactional
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }
}

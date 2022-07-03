package ru.shcherbatykh.services;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.classes.Role;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.repositories.UserRepository;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserServiceImpl(UserRepository userRepository, @Lazy BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override @Transactional
    public List<User> users() {
        return userRepository.findAll();
    }

    @Override @Transactional
    public void addUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override @Transactional
    public User getUser(long id) {
        return userRepository.getUserById(id);
    }

    @Override @Transactional
    public User getUser(String id) {
        return userRepository.getUserById(Long.valueOf(id));
    }

    @Override @Transactional
    public User findByUsername(String username){
        return userRepository.findByUsername(username).orElse(null);
    }
}

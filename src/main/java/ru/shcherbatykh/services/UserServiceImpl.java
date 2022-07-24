package ru.shcherbatykh.services;

import org.apache.log4j.Logger;
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
    private static final Logger logger = Logger.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository, @Lazy BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override @Transactional
    public List<User> users() {
        logger.debug("Method 'users' started working.");
        return userRepository.findAll();
    }

    @Override @Transactional
    public void addUser(User user) {
        logger.debug("Method 'addUser' started working.");
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        logger.info("Added new user with id=" + user.getId());
    }

    @Override @Transactional
    public User getUser(long id) {
        logger.debug("Method 'getUser' started working.");
        return userRepository.getUserById(id);
    }

    @Override @Transactional
    public User getUser(String id) {
        logger.debug("Method 'getUser' started working.");
        return userRepository.getUserById(Long.valueOf(id));
    }

    @Override @Transactional
    public User findByUsername(String username){
        logger.debug("Method 'findByUsername' started working.");
        return userRepository.findByUsername(username).orElse(null);
    }
}

package ru.shcherbatykh.security;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.services.UserService;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;
    private static final Logger logger = Logger.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Method 'loadUserByUsername' started working.");

        User user = userService.findByUsername(username);
        if (user == null)  throw new UsernameNotFoundException(username);
        return SecurityUser.fromUser(user);
    }
}

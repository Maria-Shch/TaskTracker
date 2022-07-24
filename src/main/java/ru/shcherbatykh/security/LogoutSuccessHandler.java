package ru.shcherbatykh.security;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.services.TaskService;
import ru.shcherbatykh.services.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class LogoutSuccessHandler  extends SimpleUrlLogoutSuccessHandler {

    private final UserService userService;
    private final TaskService taskService;
    private static final Logger logger = Logger.getLogger(LogoutSuccessHandler.class);

    public LogoutSuccessHandler(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.debug("Method 'onLogoutSuccess' started working.");

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByUsername(userDetails.getUsername());

        // When the user logs out from the application, it is necessary to deactivate his active task
        taskService.deactivateActiveTaskUser(user);
        super.setDefaultTargetUrl("/auth/login");
        super.onLogoutSuccess(request, response, authentication);
        logger.info("User with id=" + user.getId() + " has logged out");
    }
}

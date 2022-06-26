package ru.shcherbatykh.security;

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

    public LogoutSuccessHandler(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByUsername(userDetails.getUsername());

        // When the user exits the application, it is necessary to deactivate his active task
        taskService.deactivateActiveUserTask(user);
        super.setDefaultTargetUrl("/auth/login");
        super.onLogoutSuccess(request, response, authentication);
    }
}

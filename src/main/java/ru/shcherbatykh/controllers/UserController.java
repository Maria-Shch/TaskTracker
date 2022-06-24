package ru.shcherbatykh.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.services.TaskService;
import ru.shcherbatykh.services.UserService;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final TaskService taskService;

    public UserController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasAuthority('USER')")
    public String getUserTasksPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Task> tasks = taskService.getTasksAssignedToUser(user);

        Collections.sort(tasks, (task1, task2) -> {
            if (task1.getStatus() == task2.getStatus()) {
                return task1.getTitle().compareTo(task2.getTitle());
            } else {
                return task1.getStatus().compareTo(task2.getStatus());
            }
        });

        model.addAttribute("tasks", tasks);
        return "userTasks";
    }
}

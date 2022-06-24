package ru.shcherbatykh.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.shcherbatykh.classes.Status;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.services.TaskService;
import ru.shcherbatykh.services.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/task")
public class TaskController {
    private final UserService userService;
    private final TaskService taskService;

    public TaskController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    @GetMapping("/{id}/disactivate")
    public String taskDisactivate(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable long id) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Task> tasks= taskService.getTasksAssignedToUser(user);
        taskService.setActivityStatus(id, false);
        model.addAttribute("tasks", tasks);
        return "redirect:/user/tasks";
    }

    @GetMapping("/{id}/activate")
    public String taskActivate(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable long id) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Task> tasks = taskService.getTasksAssignedToUser(user);

        List<Task> activeTasks = tasks.stream()
                .filter(task -> task.isActivityStatus())
                .collect(Collectors.toList());

        activeTasks.stream().forEach(task -> taskService.setActivityStatus(task.getId(), false));
        taskService.setActivityStatus(id, true);

        if(taskService.getTask(id).getStatus() == Status.ACCEPTED) taskService.setStatus(id, Status.IN_PROGRESS);

        model.addAttribute("tasks", tasks);
        return "redirect:/user/tasks";
    }

    @GetMapping("/{id}/accept")
    public String taskAccept(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable long id) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Task> tasks = taskService.getTasksAssignedToUser(user);

        taskService.setStatus(id, Status.ACCEPTED);

        model.addAttribute("tasks", tasks);
        return "redirect:/user/tasks";
    }
}

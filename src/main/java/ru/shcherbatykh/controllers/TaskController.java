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
        taskService.updateActivityStatus(id, user,false);
        model.addAttribute("tasks", tasks);
        return "redirect:/user/tasks";
    }

    @GetMapping("/{id}/activate")
    public String taskActivate(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable long id) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Task> tasks = taskService.getTasksAssignedToUser(user);

        // When the user sets the activity status of the task to "true" (pressed the button "Activate"),
        // it means that he has started working on this task. At one moment,
        // the user can work on only one task -
        // you should check if there is already an active task among his tasks.
        // If it exists, you should make it inactive and only after that activate the task selected by the user.
        taskService.deactivateActiveUserTask(user);

        taskService.updateActivityStatus(id, user, true);

        // If the user activated the task with the status "accepted",
        // it means that he started working on it for the first time
        // and after that it is required to change its status from "ACCEPTED" to "IN PROGRESS"
        if(taskService.getTask(id).getStatus() == Status.ACCEPTED) taskService.updateStatus(id, user, Status.IN_PROGRESS);

        model.addAttribute("tasks", tasks);
        return "redirect:/user/tasks";
    }

    @GetMapping("/{id}/accept")
    public String taskAccept(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable long id) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Task> tasks = taskService.getTasksAssignedToUser(user);

        taskService.updateStatus(id, user, Status.ACCEPTED);

        model.addAttribute("tasks", tasks);
        return "redirect:/user/tasks";
    }
}

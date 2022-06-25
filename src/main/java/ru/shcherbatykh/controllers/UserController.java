package ru.shcherbatykh.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.shcherbatykh.classes.Status;
import ru.shcherbatykh.models.Comment;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.services.CommentService;
import ru.shcherbatykh.services.TaskService;
import ru.shcherbatykh.services.UserService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final TaskService taskService;
    private final CommentService commentService;

    public UserController(UserService userService, TaskService taskService, CommentService commentService) {
        this.userService = userService;
        this.taskService = taskService;
        this.commentService = commentService;
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasAuthority('USER')")
    public String getUserTasksPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Task> tasks = taskService.getTasksAssignedToUser(user);

        // This comparator sorts the list of tasks in the following way:
        // the first task is the one with the activity status 'true'.
        // Then all tasks with activity status "false" are sorted by ordinal of enum Status
        // If two tasks have the same ordinal are sorted by title
        Collections.sort(tasks, (task1, task2) -> {
            if (task1.isActivityStatus() == true) return -1;
            if (task2.isActivityStatus() == true) return 1;
            if (task1.getStatus() == task2.getStatus()) {
                return task1.getTitle().compareTo(task2.getTitle());
            } else {
                return task1.getStatus().compareTo(task2.getStatus());
            }
        });

        model.addAttribute("tasks", tasks);
        return "userTasks";
    }

    @GetMapping("/task/{id}")
    public String getTaskPage(Model model, @PathVariable long id) {
        Task task = taskService.getTask(id);
        List<Comment> comments = commentService.getCommentsByTask(task);
        comments.sort(Comparator.comparing(Comment::getDate));
        List<Status> statuses = new ArrayList<>();
        if(task.getStatus() == Status.IN_PROGRESS) statuses.add(Status.DONE);

        model.addAttribute("statuses", statuses);
        model.addAttribute("task", task);
        model.addAttribute("comments", comments);
        return "task";
    }

    @PostMapping("/task/{id}/changeStatus")
    public String chageStatusTask(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long id, @Valid Status selectedStatus) {
        User user = userService.findByUsername(userDetails.getUsername());
        taskService.updateStatus(id, user, selectedStatus);
        return "redirect:/user/task/{id}";
    }
}

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static ru.shcherbatykh.utils.CommandUtils.sortTasksByStatus;

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

    @GetMapping("/assignedTasks")
    @PreAuthorize("hasAuthority('USER')")
    public String getAssignedUserTasksPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Task> tasks = taskService.getTasksAssignedToUser(user);
        sortTasksByStatus(tasks);
        model.addAttribute("tasks", tasks);
        return "assignedUserTasks";
    }

    @GetMapping("/createdTasks")
    @PreAuthorize("hasAuthority('USER')")
    public String getCreatedUserTasksPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Task> tasks = taskService.getTasksCreatedByUser(user);
        sortTasksByStatus(tasks);
        model.addAttribute("tasks", tasks);
        model.addAttribute("user", user);
        return "createdUserTasks";
    }

    @GetMapping("/task/{id}")
    public String getTaskPage(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable long id) {
        User user = userService.findByUsername(userDetails.getUsername());
        Task task = taskService.getTask(id);
        List<Comment> comments = commentService.getCommentsByTask(task);
        comments.sort(Comparator.comparing(Comment::getDate));
        List<Status> statuses = new ArrayList<>();
        if(task.getStatus() == Status.IN_PROGRESS) statuses.add(Status.DONE);
        String textComment = "";
        String pathWhereReturnUser = "";

        model.addAttribute("statuses", statuses);
        model.addAttribute("task", task);
        model.addAttribute("comments", comments);
        model.addAttribute("textComment", textComment);
        model.addAttribute("user", user);
        return "task";
    }

    @PostMapping("/task/{id}/changeStatus")
    public String changeStatusTask(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long id, @Valid Status selectedStatus) {
        User user = userService.findByUsername(userDetails.getUsername());
        taskService.updateStatus(id, user, selectedStatus);
        return "redirect:/user/task/{id}";
    }
}

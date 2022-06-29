package ru.shcherbatykh.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.services.CommentService;
import ru.shcherbatykh.services.TaskService;
import ru.shcherbatykh.services.UserService;


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

    @GetMapping("/account")
    public String getAccountPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        return "account";
    }

    @GetMapping("/{id}")
    public String getUserPage(@PathVariable long id, Model model) {
        User user = userService.getUser(id);
        model.addAttribute("user", user);
        return "account";
    }
}

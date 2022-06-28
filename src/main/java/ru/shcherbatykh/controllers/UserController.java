package ru.shcherbatykh.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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

}

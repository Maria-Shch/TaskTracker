package ru.shcherbatykh.controllers;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.services.UserService;


@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private static final Logger logger = Logger.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{idUser}")
    public String getUserPage(@PathVariable long idUser, Model model) {
        logger.debug("Method 'getUserPage' started working.");

        User user = userService.getUser(idUser);
        model.addAttribute("user", user);
        return "account";
    }
}

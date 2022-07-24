package ru.shcherbatykh.controllers;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.shcherbatykh.config.SecurityConfig;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.services.UserService;
import ru.shcherbatykh.validator.UserValidator;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserValidator userValidator;
    private static final Logger logger = Logger.getLogger(AuthController.class);

    public AuthController(UserService userService, UserValidator userValidator) {
        this.userService = userService;
        this.userValidator = userValidator;
    }

    @GetMapping("/login")
    public String getLoginPage() {
        logger.debug("Method 'getLoginPage' started working.");
        return "auth/login";
    }

    @GetMapping("/failureLogin")
    public String getFailureLoginPage() {
        logger.debug("Method 'getFailureLoginPage' started working.");
        return "auth/failureLogin";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        logger.debug("Method 'registration' with @GetMapping started working.");
        model.addAttribute("newUser", new User());
        return "auth/registration";
    }

    @PostMapping("/registration")
    public String registration(@ModelAttribute("newUser") User newUser, BindingResult bindingResult) {
        logger.debug("Method 'registration' with @PostMapping started working.");
        userValidator.validate(newUser, bindingResult);

        if (bindingResult.hasErrors()) {
            return "auth/registration";
        }
        userService.addUser(newUser);
        return "auth/login";
    }
}

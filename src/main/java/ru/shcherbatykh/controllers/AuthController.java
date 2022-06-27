package ru.shcherbatykh.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.services.UserService;
import ru.shcherbatykh.validator.UserValidator;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserValidator userValidator;

    public AuthController(UserService userService, UserValidator userValidator) {
        this.userService = userService;
        this.userValidator = userValidator;
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "auth/login";
    }

    @GetMapping("/failureLogin")
    public String getFailureLoginPage() {
        return "auth/failureLogin";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("newUser", new User());
        return "auth/registration";
    }

    @PostMapping("/registration")
    public String registration(@ModelAttribute("newUser") User newUser, BindingResult bindingResult) {
        userValidator.validate(newUser, bindingResult);

        if (bindingResult.hasErrors()) {
            return "registration";
        }
        userService.addUser(newUser);
        return "auth/login";
    }
}

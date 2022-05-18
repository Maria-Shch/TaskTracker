package ru.shcherbatykh.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
        return "login";
    }

    @GetMapping("/success")
    public String getSuccessPage() {
        return "success";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("userForm", new User());
        return "registration";
    }

    @PostMapping("/registration")
    public String registration(@ModelAttribute("userForm") User newUser, BindingResult bindingResult) {
        userValidator.validate(newUser, bindingResult);

        if (bindingResult.hasErrors()) {
            return "registration"; //todo something that say user about repeatable username
        }
        userService.addUser(newUser);
        return "login";
    }



    @GetMapping("/userRolePage")
    @PreAuthorize("hasAuthority('USER')")
    public String getUserRolePage() {
        return "userRolePage";
    }


    @GetMapping("/adminRolePage")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String getAdminRolePage() {
        return "adminRolePage";
    }

    @GetMapping("/allRoles")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public String getAllRolesPage() {
        return "allRoles";
    }
}

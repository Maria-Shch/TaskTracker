package ru.shcherbatykh.validator;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.services.UserService;
import ru.shcherbatykh.tracker.TrackerApplication;

@Component
public class UserValidator implements Validator {

    private final UserService userService;
    private static final Logger logger = Logger.getLogger(UserValidator.class);

    public UserValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        logger.debug("Method 'validate' started working.");

        User user = (User) o;
        if (userService.findByUsername(user.getUsername()) != null) {
            errors.rejectValue("username", "Duplicate.newUser.username");
        }
        if (!user.getConfirmPassword().equals(user.getPassword())) {
            errors.rejectValue("confirmPassword", "Different.newUser.password");
        }
    }
}

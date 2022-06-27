package ru.shcherbatykh.controllers;

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
import java.util.List;

import static ru.shcherbatykh.utils.CommandUtils.convertLocalDateTimeFromString;
import static ru.shcherbatykh.utils.CommandUtils.sortUsersByLastnameAndName;

@Controller
@RequestMapping("/task")
public class TaskController {
    private final UserService userService;
    private final TaskService taskService;
    private final CommentService commentService;

    public TaskController(UserService userService, TaskService taskService, CommentService commentService) {
        this.userService = userService;
        this.taskService = taskService;
        this.commentService = commentService;
    }

    @GetMapping("/{id}/disactivate")
    public String taskDisactivate(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable long id) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Task> tasks= taskService.getTasksAssignedToUser(user);
        taskService.updateActivityStatus(id, user,false);
        model.addAttribute("tasks", tasks);
        return "redirect:/user/assignedTasks"; //todo а если активировлаи задачу из созданных задач
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
        return "redirect:/user/assignedTasks"; //todo а если активировлаи задачу из созданных задач
    }

    @GetMapping("/{id}/accept")
    public String taskAccept(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable long id) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Task> tasks = taskService.getTasksAssignedToUser(user);

        taskService.updateStatus(id, user, Status.ACCEPTED);

        model.addAttribute("tasks", tasks);
        return "redirect:/user/assignedTasks"; //todo а если активировлаи задачу из созданных задач
    }

    @PostMapping("/{id}/addcomment")
    public String taskAddComment(@ModelAttribute("textComment") String textComment, @AuthenticationPrincipal UserDetails userDetails, @PathVariable long id) {
        Task task = taskService.getTask(id);
        User user = userService.findByUsername(userDetails.getUsername());
        Comment comment = new Comment(task, user, textComment);
        commentService.addComment(comment);
        String url = "redirect:/user/task/" + id;
        return url;
    }

    @GetMapping("/create/{idParentTask}")
    public String createTask(Model model, @PathVariable long idParentTask) {
        List<User> users = userService.users();
        sortUsersByLastnameAndName(users);
        Long idUserExecutor = 0L;
        String deadline = "";
        model.addAttribute("newTask", new Task());
        model.addAttribute("idUserExecutor", idUserExecutor);
        model.addAttribute("idParentTask", idParentTask);
        model.addAttribute("deadline", deadline);
        model.addAttribute("users", users);
        return "taskCreate";
    }

    @PostMapping("/create/{idParentTask}")
    public String createTask(@ModelAttribute("newTask") Task task, @ModelAttribute("idUserExecutor") Long idUserExecutor,
                             @ModelAttribute("deadline") String deadline, @PathVariable long idParentTask,
                             @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername());
        task.setUserCreator(user);

        System.out.println(idParentTask);

        if(idParentTask != 0L){
            Task parentTask = taskService.getTask(idParentTask);
            task.setParentTask(parentTask);
        }

        if (idUserExecutor != 0L) task.setUserExecutor(userService.getUser(idUserExecutor));
        if (deadline.length()!=0){
            LocalDateTime dateDeadline = convertLocalDateTimeFromString(deadline);
            task.setDateDeadline(dateDeadline);
        }

        taskService.addTask(task);
        return "redirect:/user/createdTasks";
    }

    @PostMapping("/{id}/changeStatus")
    public String changeStatusTask(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long id, @Valid Status selectedStatus) {
        User user = userService.findByUsername(userDetails.getUsername());
        taskService.updateStatus(id, user, selectedStatus);
        return "redirect:/user/task/{id}";
    }
}

package ru.shcherbatykh.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.shcherbatykh.classes.Role;
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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @GetMapping("/{idTask}/disactivate")
    public String taskDisactivate(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long idTask) {
        User user = userService.findByUsername(userDetails.getUsername());
        taskService.updateActivityStatus(idTask, user,false);
        return "redirect:/task/{id}";
    }

    @GetMapping("/{idTask}/activate")
    public String taskActivate(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long idTask) {
        User user = userService.findByUsername(userDetails.getUsername());

        // When the user sets the activity status of the task to "true" (pressed the button "Activate"),
        // it means that he has started working on this task. At one moment,
        // the user can work on only one task -
        // you should check if there is already an active task among his tasks.
        // If it exists, you should make it inactive and only after that activate the task selected by the user.
        taskService.deactivateActiveTaskUser(user);

        taskService.updateActivityStatus(idTask, user, true);

        // If the user activated the task with the status "accepted",
        // it means that he started working on it for the first time
        // and after that it is required to change its status from "ACCEPTED" to "IN PROGRESS"
        if(taskService.getTask(idTask).getStatus() == Status.ACCEPTED)
            taskService.updateStatus(idTask, user, Status.IN_PROGRESS);

        return "redirect:/task/{idTask}";
    }

    @GetMapping("/{idTask}/accept")
    public String taskAccept(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long idTask) {
        User user = userService.findByUsername(userDetails.getUsername());
        taskService.updateStatus(idTask, user, Status.ACCEPTED);
        return "redirect:/task/{idTask}";
    }

    @PostMapping("/{idTask}/addcomment")
    public String taskAddComment(@ModelAttribute("textComment") String textComment, @AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable long idTask) {
        Task task = taskService.getTask(idTask);
        User user = userService.findByUsername(userDetails.getUsername());
        Comment comment = new Comment(task, user, textComment);
        commentService.addComment(comment);
        return "redirect:/task/{idTask}";
    }

    @GetMapping("/create/{idParentTask}")
    public String createTask(Model model, @PathVariable long idParentTask) {
        List<User> users = userService.users();
        sortUsersByLastnameAndName(users);

        model.addAttribute("newTask", new Task());
        model.addAttribute("idParentTask", idParentTask);
        model.addAttribute("users", users);
        return "taskCreate";
    }

    @PostMapping("/create/{idParentTask}")
    public String createTask(@ModelAttribute("newTask") Task task, @ModelAttribute("idUserExecutor") Long idUserExecutor,
                             @ModelAttribute("deadline") String deadline, @PathVariable long idParentTask,
                             @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername());
        task.setUserCreator(user);

        if(idParentTask != 0L){
            Task parentTask = taskService.getTask(idParentTask);
            task.setParentTask(parentTask);
        }

        if (idUserExecutor != 0L)
            task.setUserExecutor(userService.getUser(idUserExecutor));

        if (deadline.length()!= 0){
            LocalDateTime dateDeadline = convertLocalDateTimeFromString(deadline);
            task.setDateDeadline(dateDeadline);
        }

        taskService.addTask(task);
        return "redirect:/task/createdTasks";
    }

    @PostMapping("/{idTask}/changeStatus")
    public String changeStatusTask(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long id, @Valid Status selectedStatus) {
        User user = userService.findByUsername(userDetails.getUsername());
        Task task = taskService.getTask(id);
        if(task.getStatus() != selectedStatus){
            taskService.updateStatus(id, user, selectedStatus);
        }
        return "redirect:/task/{idTask}";
    }

    @GetMapping("/{idTask}/canceled")
    public String changeStatusTaskCanceled(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long idTask) {
        User user = userService.findByUsername(userDetails.getUsername());
        taskService.updateStatus(idTask, user, Status.CANCELED);
        return "redirect:/task/{idTask}";
    }

    @GetMapping("/allTasks/{typeTasks}")
    public String getAllTasksPage(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable String typeTasks) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Task> tasks = taskService.getTasksInHierarchicalOrder();

        model.addAttribute("typeTasks", typeTasks);
        model.addAttribute("tasks", tasks);
        model.addAttribute("user", user);
        model.addAttribute("title", "All tasks");
        return "tasks";
    }

    @GetMapping("/assignedTasks")
    public String getAssignedUserTasksPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Task> tasks = taskService.getTasksInHierarchicalOrder();
        List<Task> resultTaskList = tasks.stream()
                .filter(task -> Objects.equals(task.getUserExecutor(), user))
                .collect(Collectors.toList());

        model.addAttribute("tasks", resultTaskList);
        model.addAttribute("user", user);
        model.addAttribute("title", "Tasks assigned to you");
        return "tasks";
    }

    @GetMapping("/createdTasks")
    public String getCreatedUserTasksPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Task> tasks = taskService.getTasksInHierarchicalOrder();
        List<Task> resultTaskList = tasks.stream()
                .filter(task -> Objects.equals(task.getUserCreator(), user))
                .collect(Collectors.toList());

        model.addAttribute("tasks", resultTaskList);
        model.addAttribute("user", user);
        model.addAttribute("title", "Tasks created by you");
        return "tasks";
    }

    @GetMapping("/{idTask}")
    public String getTaskPage(@AuthenticationPrincipal UserDetails userDetails, Model model, @PathVariable long idTask) {
        User user = userService.findByUsername(userDetails.getUsername());
        Task task = taskService.getTask(idTask);
        List<Comment> comments = commentService.getCommentsByTask(task);
        comments.sort(Comparator.comparing(Comment::getDate));
        List<Status> statuses = new ArrayList<>();

        if(user.getRole() == Role.USER){
            // A user to whom the task is assigned can change its status from status IN_PROGRESS to status DONE
            if(task.getStatus() == Status.IN_PROGRESS) statuses.add(Status.DONE);

            // A user with the role USER can change the status a task to CANCELED if he is its CREATOR
            if(task.getUserCreator().getId() == user.getId() && task.getStatus() != Status.CANCELED)
                statuses.add(Status.CANCELED);
        }

        if(user.getRole() == Role.ADMIN){
            statuses.add(Status.IN_PROGRESS);
            statuses.add(Status.DONE);
            statuses.add(Status.CANCELED);
        }

        model.addAttribute("statuses", statuses);
        model.addAttribute("task", task);
        model.addAttribute("comments", comments);
        model.addAttribute("user", user);
        model.addAttribute("users", userService.users());
        return "task";
    }

    @PostMapping("/{idTask}/changeDueDate")
    public String changeDueDateTask(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long idTask,
                                    @ModelAttribute("newDueDate") String newDueDate) {
        User user = userService.findByUsername(userDetails.getUsername());

        // newDueDate length can be 0 when the user tried to change due date
        // without selecting it and just clicked the button 'change'
        if (newDueDate.length() != 0) {
            LocalDateTime newDate = convertLocalDateTimeFromString(newDueDate);
            taskService.updateDateDeadline(idTask, user, newDate);
        }
        return "redirect:/task/{idTask}";
    }

    @PostMapping("/{idTask}/changeUserExecutor")
    public String changeDueDateTask(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long idTask,
                                    @ModelAttribute("idNewUserExecutor") long idNewUserExecutor) {
        User user = userService.findByUsername(userDetails.getUsername());

        // idNewUserExecutor can be 0 when the user tried to change user executor
        // without selecting it and just clicked the button 'change'
        if(idNewUserExecutor != 0) {
            // If someone changed the user executor while someone else was working on this task,
            // then you need to make this task inactive for the previous user executor and
            // only after that change the user executor
            Task task = taskService.getTask(idTask);
            if(task.isActivityStatus())
                taskService.updateActivityStatus(idTask, task.getUserExecutor(), false);
            taskService.updateUserExecutor(idTask, user, userService.getUser(idNewUserExecutor));
        }
        return "redirect:/task/{idTask}";
    }
}

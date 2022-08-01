package ru.shcherbatykh.controllers;

import org.apache.log4j.Logger;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.shcherbatykh.models.History;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.services.HistoryService;
import ru.shcherbatykh.services.StatisticalService;
import ru.shcherbatykh.services.TaskService;
import ru.shcherbatykh.services.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static ru.shcherbatykh.utils.CommandUtils.*;

@Controller
@RequestMapping("/statistics")
public class StatisticsController {

    private final UserService userService;
    private final StatisticalService statisticalService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private static final Logger logger = Logger.getLogger(StatisticsController.class);

    public StatisticsController(UserService userService, StatisticalService statisticalService, TaskService taskService,
                                HistoryService historyService) {
        this.userService = userService;
        this.statisticalService = statisticalService;
        this.taskService = taskService;
        this.historyService = historyService;
    }

    @GetMapping("/{idSelectedUser}")
    public String getStatistics(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable long idSelectedUser,
                                Model model) {

        logger.debug("Method 'getStatistics' with @GetMapping started working.");

        User loggedInUser = userService.findByUsername(userDetails.getUsername());
        User selectedUser = userService.getUser(idSelectedUser);

        List<User> users = userService.users();
        sortUsersByLastnameAndName(users);

        model.addAttribute("user", loggedInUser);
        model.addAttribute("users", users);
        model.addAttribute("selectedUser", selectedUser);
        model.addAttribute("tasks", taskService.getTasksUserHasEverWorkedOn(selectedUser));
        model.addAttribute("isResponse", false);
        return "statistics";
    }

    @PostMapping("/{idSelectedUser}")
    public String getStatistics(@AuthenticationPrincipal UserDetails userDetails, Model model,
                                @PathVariable long idSelectedUser,
                                @ModelAttribute("startPeriod") String startPeriod,
                                @ModelAttribute("finishPeriod") String finishPeriod,
                                @ModelAttribute("idTask") String idTask,
                                @RequestParam("isTaskWithChildren") boolean isTaskWithChildren) {

        logger.debug("Method 'getStatistics' with @PostMapping started working.");

        User loggedInUser = userService.findByUsername(userDetails.getUsername());
        User selectedUser = userService.getUser(idSelectedUser);

        LocalDateTime startPeriodLDT;
        LocalDateTime finishPeriodLDT;

        if (startPeriod.length() == 0)
            startPeriodLDT = historyService.getTheEarliestDateOfActivityStatusChangeByUser(selectedUser);
        else
            startPeriodLDT = convertLocalDateTimeFromString(startPeriod);


        if (finishPeriod.length() == 0)
            finishPeriodLDT = historyService.getTheLatestDateOfActivityStatusChangeByUser(selectedUser);
        else
            finishPeriodLDT = convertLocalDateTimeFromString(finishPeriod);


        if(startPeriodLDT == null || finishPeriodLDT == null){
            if(idTask.equals("all"))
                model.addAttribute("selectedTask", "all");
            else
                model.addAttribute("selectedTask", taskService.getTask(Long.valueOf(idTask)));

            model.addAttribute("user", loggedInUser);
            model.addAttribute("selectedUser", selectedUser);
            model.addAttribute("elapsedTimeAsString", null);
            model.addAttribute("startPeriod", startPeriodLDT);
            model.addAttribute("finishPeriod", finishPeriodLDT);
            model.addAttribute("tasks", taskService.getTasks());
            model.addAttribute("isTaskWithChildren", isTaskWithChildren);
            model.addAttribute("isResponse", true);
            model.addAttribute("isUserSelected", false);
            return "statistics";
        }

        if (startPeriodLDT.isAfter(finishPeriodLDT)) {
            model.addAttribute("exceptionStartAfterFinish",
                    "The selected finish date of the period is earlier than the start date of the period");
            model.addAttribute("user", loggedInUser);
            model.addAttribute("tasks", selectedUser.getTasksAssignedToUser());
            return "statistics";
        }

        List<Task> tasksForStatisticsAnalysis = taskService.getTasksForStatisticsAnalysis(idTask, selectedUser, isTaskWithChildren,
                startPeriodLDT, finishPeriodLDT);

        Long elapsedTimeInMilliseconds = 0L;

        if(tasksForStatisticsAnalysis.isEmpty()){
            History history = historyService.checkUserWorkingInPeriod(selectedUser, startPeriodLDT);
            if(history != null){
                tasksForStatisticsAnalysis.add(history.getTask());
                elapsedTimeInMilliseconds = ChronoUnit.MILLIS.between(startPeriodLDT, finishPeriodLDT);
            }
        }
        else{
            elapsedTimeInMilliseconds = statisticalService.getTimeInMillisSpentByUserOnTaskForPeriod
                    (tasksForStatisticsAnalysis, selectedUser, startPeriodLDT, finishPeriodLDT, isTaskWithChildren);
        }

        String elapsedTimeAsString = null;
        if(elapsedTimeInMilliseconds != 0)
            elapsedTimeAsString = convertPeriodOfTimeToString(elapsedTimeInMilliseconds);


        if(idTask.equals("all"))
            model.addAttribute("selectedTask", "all");
        else
            model.addAttribute("selectedTask", taskService.getTask(Long.valueOf(idTask)));

        model.addAttribute("user", loggedInUser);
        model.addAttribute("selectedUser", selectedUser);
        model.addAttribute("tasksForStatisticsAnalysis", tasksForStatisticsAnalysis);
        model.addAttribute("elapsedTimeAsString", elapsedTimeAsString);
        model.addAttribute("startPeriod", startPeriodLDT);
        model.addAttribute("finishPeriod", finishPeriodLDT);
        model.addAttribute("tasks", taskService.getTasksUserHasEverWorkedOn(selectedUser));
        model.addAttribute("isTaskWithChildren", isTaskWithChildren);
        model.addAttribute("isResponse", true);
        model.addAttribute("isUserSelected", false);
        return "statistics";
    }

    @GetMapping("/selectUser")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String selectUser(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        logger.debug("Method 'selectUser' with @GetMapping started working.");
        model.addAttribute("users", userService.users());
        model.addAttribute("user", userService.findByUsername(userDetails.getUsername()));
        return "selectUser";
    }

    @PostMapping("/selectUser")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String selectUser(@ModelAttribute("idUser") String idSelectedUser) {
        logger.debug("Method 'selectUser' with @PostMapping started working.");
        return "redirect:/statistics/" + idSelectedUser;
    }
}

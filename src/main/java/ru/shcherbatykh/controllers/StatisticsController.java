package ru.shcherbatykh.controllers;

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

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static ru.shcherbatykh.utils.CommandUtils.convertLocalDateTimeFromString;
import static ru.shcherbatykh.utils.CommandUtils.convertPeriodOfTimeToString;

@Controller
@RequestMapping("/statistics")
public class StatisticsController {
    private final UserService userService;
    private final StatisticalService statisticalService;
    private final TaskService taskService;
    private final HistoryService historyService;

    public StatisticsController(UserService userService, StatisticalService statisticalService, TaskService taskService,
                                HistoryService historyService) {
        this.userService = userService;
        this.statisticalService = statisticalService;
        this.taskService = taskService;
        this.historyService = historyService;
    }

    @GetMapping("")
    public String getStatistics(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("tasks", taskService.getTasksUserHasEverWorkedOn(user));
        model.addAttribute("isResponse", false);
        return "statistics";
    }

    //todo clear sout
    @PostMapping("")
    public String getStatistics(@AuthenticationPrincipal UserDetails userDetails, Model model,
                                @ModelAttribute("startPeriod") String startPeriod,
                                @ModelAttribute("finishPeriod") String finishPeriod,
                                @ModelAttribute("idTask") String idTask,
                                @RequestParam("isTaskWithChildren") boolean isTaskWithChildren) {

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(isTaskWithChildren);

        User user = userService.findByUsername(userDetails.getUsername());
        System.out.println("USER " + user);

        LocalDateTime startPeriodLDT;
        LocalDateTime finishPeriodLDT;

        if (startPeriod.length() == 0) {
            startPeriodLDT = historyService.getTheEarliestDateOfActivityStatusChangeByUser(user);
            System.out.println("startPeriod.length() == 0 ==> " + startPeriodLDT);
        }
        else {
            startPeriodLDT = convertLocalDateTimeFromString(startPeriod);
            System.out.println("startPeriod was selected ==> " + startPeriodLDT);
        }

        if (finishPeriod.length() == 0) {
            finishPeriodLDT = historyService.getTheLatestDateOfActivityStatusChangeByUser(user);
            System.out.println("finishPeriod.length() == 0 ==> " + finishPeriodLDT);
        }
        else {
            finishPeriodLDT = convertLocalDateTimeFromString(finishPeriod);
            System.out.println("finishPeriod was selected ==> " + finishPeriodLDT);
        }

        if(startPeriodLDT == null || finishPeriodLDT == null){
            if(idTask.equals("all")){
                model.addAttribute("selectedTask", "all");
            } else model.addAttribute("selectedTask", taskService.getTask(Long.valueOf(idTask)));

            model.addAttribute("user", user);
            model.addAttribute("elapsedTimeAsString", null);
            model.addAttribute("startPeriod", startPeriodLDT);
            model.addAttribute("finishPeriod", finishPeriodLDT);
            model.addAttribute("tasks", taskService.getTasks());
            model.addAttribute("isTaskWithChildren", isTaskWithChildren);
            model.addAttribute("isResponse", true);
            return "statistics";
        }

        if (startPeriodLDT.isAfter(finishPeriodLDT)) {
            model.addAttribute("exceptionStartAfterFinish",
                    "The selected finish date of the period is earlier than the start date of the period");
            System.out.println("startPeriodLDT.isAfter(finishPeriodLDT)");
            model.addAttribute("user", user);
            model.addAttribute("tasks", user.getTasksAssignedToUser());
            return "statistics";
        }

        List<Task> tasksForStatisticsAnalysis = taskService.getTasksForStatisticsAnalysis(idTask, user, isTaskWithChildren,
                startPeriodLDT, finishPeriodLDT);

        Long elapsedTimeInMilliseconds = 0L;

        if(tasksForStatisticsAnalysis.isEmpty()){
            History history = historyService.checkUserWorkingInPeriod(user, startPeriodLDT);
            if(history != null){
                tasksForStatisticsAnalysis.add(history.getTask());
                elapsedTimeInMilliseconds = ChronoUnit.MILLIS.between(startPeriodLDT, finishPeriodLDT);
            }
        }
        else{
            elapsedTimeInMilliseconds = statisticalService.getTimeInMillisSpentByUserOnTaskForPeriod
                    (tasksForStatisticsAnalysis, user, startPeriodLDT, finishPeriodLDT, isTaskWithChildren);
        }

        String elapsedTimeAsString = null;
        if(elapsedTimeInMilliseconds != 0){
            elapsedTimeAsString = convertPeriodOfTimeToString(elapsedTimeInMilliseconds);
            System.out.println("elapsedTimeInMilliseconds != 0 " + elapsedTimeAsString);
        } else{
            System.out.println("elapsedTimeInMilliseconds == 0");
        }


        if(idTask.equals("all")){
            model.addAttribute("selectedTask", "all");
        } else model.addAttribute("selectedTask", taskService.getTask(Long.valueOf(idTask)));

        model.addAttribute("user", user);
        model.addAttribute("tasksForStatisticsAnalysis", tasksForStatisticsAnalysis);
        model.addAttribute("elapsedTimeAsString", elapsedTimeAsString);
        model.addAttribute("startPeriod", startPeriodLDT);
        model.addAttribute("finishPeriod", finishPeriodLDT);
        model.addAttribute("tasks", taskService.getTasksUserHasEverWorkedOn(loggedInUser));
        model.addAttribute("isTaskWithChildren", isTaskWithChildren);
        model.addAttribute("isResponse", true);
        return "statistics";
    }
}

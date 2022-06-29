package ru.shcherbatykh.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.shcherbatykh.classes.TypeEvent;
import ru.shcherbatykh.classes.UpdatableTaskField;
import ru.shcherbatykh.models.History;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.services.CommentService;
import ru.shcherbatykh.services.HistoryService;
import ru.shcherbatykh.services.TaskService;
import ru.shcherbatykh.services.UserService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.reverseOrder;
import static ru.shcherbatykh.utils.CommandUtils.convertLocalDateTimeFromStringToString;

@Controller
@RequestMapping("/history")
public class HistoryController {
    private final UserService userService;
    private final HistoryService historyService;
    private final TaskService taskService;
    private final CommentService commentService;

    public HistoryController(UserService userService, HistoryService historyService, TaskService taskService, CommentService commentService) {
        this.userService = userService;
        this.historyService = historyService;
        this.taskService = taskService;
        this.commentService = commentService;
    }

    @GetMapping("")
    public String getHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        String idUser = String.valueOf(user.getId());
        List<History> histories = historyService.getHistoriesOfLastThreeDays();

        List<History> resultList = Stream.of(
                    historyService.getAssignedTasks(histories, idUser),
                    historyService.getStartedWork(histories, user),
                    historyService.getStoppedWork(histories, user),
                    historyService.getChangedDeadlineByExecutor(histories, user),
                    historyService.getChangedStatusByExecutor(histories, user),
                    historyService.getChangedStatusForTaskCreatedByUser(histories, user),
                    historyService.getChangedDeadlineForTaskCreatedByUser(histories, user),
                    historyService.getChangeAssignedUserForTaskCreatedByUser(histories, user))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Collections.sort(resultList, Comparator.comparing(History::getDate, reverseOrder()));

        model.addAttribute("user", user);
        model.addAttribute("resultList", resultList);
        return "history";
    }
}
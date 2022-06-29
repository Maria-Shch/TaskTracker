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

        List<History> assignedTasks = histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.ID_USER_EXECUTOR
                        && history.getNewValue().equals(idUser))
                .map(history -> {
                    history.setTypeEvent(TypeEvent.ASSIGNED_TASK);
                    return history;
                })
                .collect(Collectors.toList());

        List<History> startedWork = histories.stream()
                .filter(history -> history.getUserWhoUpdated() == user
                        && history.getTaskField() == UpdatableTaskField.ACTIVITY_STATUS
                        && history.getOldValue().equals("false"))
                .map(history -> {
                    history.setTypeEvent(TypeEvent.STARTED_WORK);
                    return history;
                })
                .collect(Collectors.toList());


        List<History> stoppedWork = histories.stream()
                .filter(history -> history.getUserWhoUpdated() == user
                        && history.getTaskField() == UpdatableTaskField.ACTIVITY_STATUS
                        && history.getOldValue().equals("true"))
                .map(history -> {
                    history.setTypeEvent(TypeEvent.STOPPED_WORK);
                    return history;
                })
                .collect(Collectors.toList());

        List<History> changedDeadlineByExecutor = histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.DATE_DEADLINE
                        && history.getTask().getUserExecutor() != null
                        && history.getTask().getUserExecutor().getId() == user.getId())
                .map(history -> {
                    history.setTypeEvent(TypeEvent.CHANGE_STATUS_BY_EXECUTOR);
                    return history;
                })
                .collect(Collectors.toList());

        List<History> changedStatusByExecutor = histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.STATUS
                        && history.getTask().getUserExecutor() != null
                        && history.getTask().getUserExecutor().getId() == user.getId())
                .map(history -> {
                    history.setTypeEvent(TypeEvent.CHANGE_STATUS_BY_EXECUTOR);
                    return history;
                })
                .collect(Collectors.toList());

        List<History> changedStatusForTaskCreatedByUser = histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.STATUS
                        && history.getTask().getUserCreator().getId() == user.getId())
                .map(history -> {
                    history.setTypeEvent(TypeEvent.CHANGE_STATUS_FOR_TASK_CREATED_BY_USER);
                    return history;
                })
                .collect(Collectors.toList());

        List<History> changedDeadlineForTaskCreatedByUser = histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.DATE_DEADLINE
                        && history.getTask().getUserCreator().getId() == user.getId())
                .map(history -> {
                    history.setTypeEvent(TypeEvent.CHANGE_DEADLINE_FOR_TASK_CREATED_BY_USER);
                    return history;
                })
                .collect(Collectors.toList());

        List<History> changeAssignedUserForTaskCreatedByUser = histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.ID_USER_EXECUTOR
                        && history.getTask().getUserCreator().getId() == user.getId())
                .map(history -> {
                    history.setTypeEvent(TypeEvent.CHANGE_ASSIGNED_USER_FOR_TASK_CREATED_BY_USER);
                    return history;
                })
                .collect(Collectors.toList());

        List<History> resultList = Stream.of(assignedTasks,
                        startedWork,
                        stoppedWork,
                        changedDeadlineByExecutor,
                        changedStatusByExecutor,
                        changedStatusForTaskCreatedByUser,
                        changedDeadlineForTaskCreatedByUser,
                        changeAssignedUserForTaskCreatedByUser)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Collections.sort(resultList, Comparator.comparing(History::getDate));

        model.addAttribute("user", user);
        model.addAttribute("resultList", resultList);
        return "history";
    }
}
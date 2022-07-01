package ru.shcherbatykh.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.shcherbatykh.classes.*;
import ru.shcherbatykh.models.History;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.services.HistoryService;
import ru.shcherbatykh.services.UserService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.reverseOrder;

@Controller
@RequestMapping("/history")
public class HistoryController {
    private final UserService userService;
    private final HistoryService historyService;

    public HistoryController(UserService userService, HistoryService historyService) {
        this.userService = userService;
        this.historyService = historyService;
    }

    @GetMapping("")
    public String getHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        String idUser = String.valueOf(user.getId());

        //By default, the history page displays history for the last 3 days
        List<History> histories = historyService.getHistoriesOfLastNDays(3);

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
        resultList = resultList.stream().distinct().collect(Collectors.toList());

        List<Period> periods = List.of(Period.ALL, Period.THIS_DAY, Period.THREE_DAYS, Period.THIS_WEEK, Period.THIS_MONTH);
        List<TaskType> taskTypes = List.of(TaskType.ALL, TaskType.ASSIGNED_TO_THIS_USER, TaskType.CREATED_BY_THIS_USER);
        List<UpdatableTaskField> changedFields = List.of(UpdatableTaskField.ALL, UpdatableTaskField.STATUS,
                UpdatableTaskField.ACTIVITY_STATUS, UpdatableTaskField.DATE_DEADLINE, UpdatableTaskField.ID_USER_EXECUTOR);

        model.addAttribute("user", user);
        model.addAttribute("resultList", resultList);
        model.addAttribute("periods", periods);
        model.addAttribute("taskTypes", taskTypes);
        model.addAttribute("changedFields", changedFields);
        model.addAttribute("sortingHistory", new SortingHistory(Period.THREE_DAYS, TaskType.ALL, UpdatableTaskField.ALL));
        return "history";
    }

    @PostMapping("")
    public String getHistories(@AuthenticationPrincipal UserDetails userDetails, @ModelAttribute("sortingHistory") SortingHistory sortingHistory, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        String idUser = String.valueOf(user.getId());

        List<History> histories = switch(sortingHistory.getPeriod()) {
            case ALL -> historyService.getHistories();
            case THIS_DAY -> historyService.getHistoriesOfLastNDays(1);
            case THREE_DAYS -> historyService.getHistoriesOfLastNDays(3);
            case THIS_WEEK -> historyService.getHistoriesOfLastNDays(7);
            case THIS_MONTH -> historyService.getHistoriesOfLastNDays(30);
        };

        List<History> resultHistories = switch(sortingHistory.getTaskType()) {
            case ALL -> Stream.of(
                        historyService.getAssignedTasks(histories, idUser), //ASSIGNED_TO_THIS_USER
                        historyService.getStartedWork(histories, user), //ASSIGNED_TO_THIS_USER
                        historyService.getStoppedWork(histories, user), //ASSIGNED_TO_THIS_USER
                        historyService.getChangedDeadlineByExecutor(histories, user), //ASSIGNED_TO_THIS_USER
                        historyService.getChangedStatusByExecutor(histories, user),  //ASSIGNED_TO_THIS_USER
                        historyService.getChangedStatusForTaskCreatedByUser(histories, user), //CREATED_BY_THIS_USER
                        historyService.getChangedDeadlineForTaskCreatedByUser(histories, user), //CREATED_BY_THIS_USER
                        historyService.getChangeAssignedUserForTaskCreatedByUser(histories, user))  //CREATED_BY_THIS_USER
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            case ASSIGNED_TO_THIS_USER ->  Stream.of(
                        historyService.getAssignedTasks(histories, idUser), //ASSIGNED_TO_THIS_USER
                        historyService.getStartedWork(histories, user), //ASSIGNED_TO_THIS_USER
                        historyService.getStoppedWork(histories, user), //ASSIGNED_TO_THIS_USER
                        historyService.getChangedDeadlineByExecutor(histories, user), //ASSIGNED_TO_THIS_USER
                        historyService.getChangedStatusByExecutor(histories, user))//ASSIGNED_TO_THIS_USER
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            case CREATED_BY_THIS_USER -> Stream.of(
                        historyService.getChangedStatusForTaskCreatedByUser(histories, user), //CREATED_BY_THIS_USER
                        historyService.getChangedDeadlineForTaskCreatedByUser(histories, user), //CREATED_BY_THIS_USER
                        historyService.getChangeAssignedUserForTaskCreatedByUser(histories, user)) //CREATED_BY_THIS_USER
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        };

        List<History> result =  switch(sortingHistory.getChangedField()) {
            case ALL -> resultHistories;
            case STATUS -> resultHistories.stream()
                    .filter(history -> history.getTaskField() == UpdatableTaskField.STATUS)
                    .collect(Collectors.toList());
            case ACTIVITY_STATUS -> resultHistories.stream()
                    .filter(history -> history.getTaskField() == UpdatableTaskField.ACTIVITY_STATUS)
                    .collect(Collectors.toList());
            case DATE_DEADLINE -> resultHistories.stream()
                    .filter(history -> history.getTaskField() == UpdatableTaskField.DATE_DEADLINE)
                    .collect(Collectors.toList());
            case ID_USER_EXECUTOR -> resultHistories.stream()
                    .filter(history -> history.getTaskField() == UpdatableTaskField.ID_USER_EXECUTOR)
                    .collect(Collectors.toList());
            default -> throw new IllegalArgumentException();
        };

        Collections.sort(result, Comparator.comparing(History::getDate, reverseOrder()));
        result = result.stream().distinct().collect(Collectors.toList());

        List<Period> periods = List.of(Period.ALL, Period.THIS_DAY, Period.THREE_DAYS, Period.THIS_WEEK, Period.THIS_MONTH);
        List<TaskType> taskTypes = List.of(TaskType.ALL, TaskType.ASSIGNED_TO_THIS_USER, TaskType.CREATED_BY_THIS_USER);
        List<UpdatableTaskField> changedFields = List.of(UpdatableTaskField.ALL, UpdatableTaskField.STATUS,
                UpdatableTaskField.ACTIVITY_STATUS, UpdatableTaskField.DATE_DEADLINE, UpdatableTaskField.ID_USER_EXECUTOR);

        model.addAttribute("user", user);
        model.addAttribute("resultList", result);
        model.addAttribute("periods", periods);
        model.addAttribute("taskTypes", taskTypes);
        model.addAttribute("changedFields", changedFields);
        model.addAttribute("sortingHistory", new SortingHistory(sortingHistory.getPeriod(), sortingHistory.getTaskType(),
                sortingHistory.getChangedField()));
        return "history";
    }
}
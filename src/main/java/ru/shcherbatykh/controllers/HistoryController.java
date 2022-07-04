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

import static java.util.Comparator.reverseOrder;
import static ru.shcherbatykh.utils.CommandUtils.sortUsersByLastnameAndNameWithMainUser;

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

        //By default, the history page displays history for the last 3 days
        List<History> histories = historyService.getHistoriesOfLastNDays(3);
        List<History> resultList = historyService.filterHistoriesByTaskType(histories,
                TaskType.TASKS_CREATED_BY_THIS_USER_OR_ASSIGNED_TO_THIS_USER, user);

        Collections.sort(resultList, Comparator.comparing(History::getDate, reverseOrder()));
        resultList = resultList.stream().distinct().collect(Collectors.toList());
        resultList = historyService.fillInFieldsOldAndNewUserExecutors(resultList);

        List<UpdatableTaskField> changedFields = List.of(UpdatableTaskField.ALL, UpdatableTaskField.STATUS,
                UpdatableTaskField.ACTIVITY_STATUS, UpdatableTaskField.DATE_DEADLINE, UpdatableTaskField.ID_USER_EXECUTOR);

        List<User> users = userService.users();
        sortUsersByLastnameAndNameWithMainUser(users, user);

        model.addAttribute("user", user);
        model.addAttribute("users", users);
        model.addAttribute("resultList", resultList);
        model.addAttribute("periods", Period.values());
        model.addAttribute("taskTypes", TaskType.values());
        model.addAttribute("changedFields", changedFields);
        model.addAttribute("selectedUser", user);
        model.addAttribute("sortingHistory", new SortingHistory(Period.THREE_DAYS,
                TaskType.TASKS_CREATED_BY_THIS_USER_OR_ASSIGNED_TO_THIS_USER, UpdatableTaskField.ALL, null));
        return "history";
    }

    @PostMapping("")
    public String getHistories(@AuthenticationPrincipal UserDetails userDetails, Model model,
                               @ModelAttribute("sortingHistory") SortingHistory sortingHistory) {
        User user = userService.findByUsername(userDetails.getUsername());
        User selectedUser = userService.getUser(sortingHistory.getIdSelectedUser());

        List<History> histories = historyService.getHistoriesOfPeriod(sortingHistory.getPeriod());
        histories = historyService.filterHistoriesByTaskType(histories, sortingHistory.getTaskType(), selectedUser);
        histories =  historyService.filterHistoriesByChangedFiled(histories, sortingHistory.getChangedField());

        Collections.sort(histories, Comparator.comparing(History::getDate, reverseOrder()));
        histories = histories.stream().distinct().collect(Collectors.toList());
        histories = historyService.fillInFieldsOldAndNewUserExecutors(histories);

        List<UpdatableTaskField> changedFields = List.of(UpdatableTaskField.ALL, UpdatableTaskField.STATUS,
                UpdatableTaskField.ACTIVITY_STATUS, UpdatableTaskField.DATE_DEADLINE, UpdatableTaskField.ID_USER_EXECUTOR);

        List<User> users = userService.users();
        sortUsersByLastnameAndNameWithMainUser(users, user);

        model.addAttribute("user", user);
        model.addAttribute("users", users);
        model.addAttribute("resultList", histories);
        model.addAttribute("periods", Period.values());
        model.addAttribute("taskTypes", TaskType.values());
        model.addAttribute("changedFields", changedFields);
        model.addAttribute("selectedUser", selectedUser);
        model.addAttribute("sortingHistory", new SortingHistory(sortingHistory.getPeriod(), sortingHistory.getTaskType(),
                sortingHistory.getChangedField(), sortingHistory.getIdSelectedUser()));
        return "history";
    }
}
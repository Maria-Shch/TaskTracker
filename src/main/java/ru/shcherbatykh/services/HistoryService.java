package ru.shcherbatykh.services;

import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.models.History;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.classes.UpdatableTaskField;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoryService {
    @Transactional
    List<History> getHistories();

    List<History> getHistoriesOfLastNDays(int countMinusDays);

    @Transactional
    List<History> historyOfTask(Task task);

    @Transactional
    void recordTaskFieldChange(Task task, User user, UpdatableTaskField field, String oldValue, String newValue);

    @Transactional
    List<History> fillInFieldsOldAndNewUserExecutors(List<History> originalList);

    @Transactional
    List<History> getHistoriesAboutChangesOfActivityStatusOfTaskByUserForPeriod
            (Task task, User user, LocalDateTime periodTimeStart, LocalDateTime periodTimeFinish);

    @Transactional
    List<History> getAssignedTasks(List<History> histories, String idUser);

    @Transactional
    List<History> getStartedWork(List<History> histories, User user);

    @Transactional
    List<History> getStoppedWork(List<History> histories, User user);

    @Transactional
    List<History> getChangedDeadlineByExecutor(List<History> histories, User user);

    @Transactional
    List<History> getChangedStatusByExecutor(List<History> histories, User user);

    @Transactional
    List<History> getChangedStatusForTaskCreatedByUser(List<History> histories, User user);

    @Transactional
    List<History> getChangedDeadlineForTaskCreatedByUser(List<History> histories, User user);

    @Transactional
    List<History> getChangeAssignedUserForTaskCreatedByUser(List<History> histories, User user);
}
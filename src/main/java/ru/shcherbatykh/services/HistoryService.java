package ru.shcherbatykh.services;

import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.classes.Period;
import ru.shcherbatykh.classes.TaskType;
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

    List<History> getHistoriesOfPeriod(Period period);

    List<History> filterHistoriesByTaskType(List<History> histories, TaskType taskType, User user);

    List<History> filterHistoriesByChangedFiled(List<History> histories, UpdatableTaskField changedField);

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
    List<History> getRecordsWhereUserWasAssignedAsExecutor(List<History> histories, User User);

    @Transactional
    List<History> getStartOfWorkOnTaskRecordsByUser(List<History> histories, User user);

    @Transactional
    List<History> getStopOfWorkOnTaskRecordsByUser(List<History> histories, User user);

    @Transactional
    List<History> getDeadlineChangeRecordsForTasksWhereUserIsAssignedAsExecutor(List<History> histories, User user);

    @Transactional
    List<History> getStatusChangeRecordsForTasksWhereUserIsAssignedAsExecutor(List<History> histories, User user);

    @Transactional
    List<History> getStatusChangeRecordsForTasksCreatedByUser(List<History> histories, User user);

    @Transactional
    List<History> getDeadlineChangeRecordsForTasksCreatedByUser(List<History> histories, User user);

    @Transactional
    List<History> getUserExecutorChangeRecordsForTasksCreatedByUser(List<History> histories, User user);
}
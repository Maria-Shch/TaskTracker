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

    @Transactional
    List<History> getHistoriesOfLastThreeDays();

    @Transactional
    List<History> historyOfTask(Task task);

    @Transactional
    void recordTaskFieldChange(Task task, User user, UpdatableTaskField field, String oldValue, String newValue);

    @Transactional
    List<History> getHistoriesAboutChangesOfActivityStatusOfTaskByUserForPeriod
            (Task task, User user, LocalDateTime periodTimeStart, LocalDateTime periodTimeFinish);
}
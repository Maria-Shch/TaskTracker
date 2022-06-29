package ru.shcherbatykh.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.classes.TypeEvent;
import ru.shcherbatykh.models.History;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.repositories.HistoryRepository;
import ru.shcherbatykh.classes.UpdatableTaskField;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoryServiceImpl implements HistoryService{

    private final HistoryRepository historyRepository;

    public HistoryServiceImpl(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override @Transactional
    public List<History> getHistories() {
        return historyRepository.findAll();
    }

    @Override
    public List<History> getHistoriesOfLastThreeDays() {
        LocalDateTime finishOfPeriod = LocalDateTime.now();
        LocalDateTime startOfPeriod =finishOfPeriod.minusDays(3);
        return historyRepository.findAllOfLastThreeDays(startOfPeriod, finishOfPeriod);
    }

    @Override @Transactional
    public List<History> historyOfTask(Task task){
        return historyRepository.findByTask(task);
    }

    @Override @Transactional
    public void recordTaskFieldChange(Task task, User userWhoUpdated, UpdatableTaskField field, String oldValue, String newValue){
        historyRepository.save(new History(task, userWhoUpdated, field, oldValue, newValue));
    }

    @Override @Transactional
    public List<History> getHistoriesAboutChangesOfActivityStatusOfTaskByUserForPeriod
            (Task task, User user, LocalDateTime periodTimeStart, LocalDateTime periodTimeFinish){
        return historyRepository.findByTaskAndUserWhoUpdatedAndTaskFieldIsAndInPeriod
                (task.getId(), user.getId(), periodTimeStart, periodTimeFinish);
    }

    @Override @Transactional
    public List<History> getAssignedTasks(List<History> histories, String idUser){
        return histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.ID_USER_EXECUTOR
                        && history.getNewValue().equals(idUser))
                .map(history -> {
                    history.setTypeEvent(TypeEvent.ASSIGNED_TASK);
                    return history;
                })
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public List<History> getStartedWork(List<History> histories, User user){
        return histories.stream()
                .filter(history -> history.getUserWhoUpdated() == user
                        && history.getTaskField() == UpdatableTaskField.ACTIVITY_STATUS
                        && history.getOldValue().equals("false"))
                .map(history -> {
                    history.setTypeEvent(TypeEvent.STARTED_WORK);
                    return history;
                })
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public List<History> getStoppedWork(List<History> histories, User user){
        return histories.stream()
                .filter(history -> history.getUserWhoUpdated() == user
                        && history.getTaskField() == UpdatableTaskField.ACTIVITY_STATUS
                        && history.getOldValue().equals("true"))
                .map(history -> {
                    history.setTypeEvent(TypeEvent.STOPPED_WORK);
                    return history;
                })
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public List<History> getChangedDeadlineByExecutor(List<History> histories, User user){
        return histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.DATE_DEADLINE
                        && history.getTask().getUserExecutor() != null
                        && history.getTask().getUserExecutor().getId() == user.getId())
                .map(history -> {
                    history.setTypeEvent(TypeEvent.CHANGE_STATUS_BY_EXECUTOR);
                    return history;
                })
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public List<History> getChangedStatusByExecutor(List<History> histories, User user){
        return histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.STATUS
                        && history.getTask().getUserExecutor() != null
                        && history.getTask().getUserExecutor().getId() == user.getId())
                .map(history -> {
                    history.setTypeEvent(TypeEvent.CHANGE_STATUS_BY_EXECUTOR);
                    return history;
                })
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public List<History> getChangedStatusForTaskCreatedByUser(List<History> histories, User user){
        return histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.STATUS
                        && history.getTask().getUserCreator().getId() == user.getId()
                        && history.getUserWhoUpdated().getId() != user.getId())
                .map(history -> {
                    history.setTypeEvent(TypeEvent.CHANGE_STATUS_FOR_TASK_CREATED_BY_USER);
                    return history;
                })
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public List<History> getChangedDeadlineForTaskCreatedByUser(List<History> histories, User user){
        return histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.DATE_DEADLINE
                        && history.getTask().getUserCreator().getId() == user.getId())
                .map(history -> {
                    history.setTypeEvent(TypeEvent.CHANGE_DEADLINE_FOR_TASK_CREATED_BY_USER);
                    return history;
                })
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public List<History> getChangeAssignedUserForTaskCreatedByUser(List<History> histories, User user){
        return histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.ID_USER_EXECUTOR
                        && history.getTask().getUserCreator().getId() == user.getId()
                        && history.getUserWhoUpdated().getId() != user.getId())
                .map(history -> {
                    history.setTypeEvent(TypeEvent.CHANGE_ASSIGNED_USER_FOR_TASK_CREATED_BY_USER);
                    return history;
                })
                .collect(Collectors.toList());
    }
}
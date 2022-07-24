package ru.shcherbatykh.services;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.classes.Period;
import ru.shcherbatykh.classes.TaskType;
import ru.shcherbatykh.classes.TypeEvent;
import ru.shcherbatykh.models.History;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.repositories.HistoryRepository;
import ru.shcherbatykh.classes.UpdatableTaskField;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class HistoryServiceImpl implements HistoryService{

    private final HistoryRepository historyRepository;
    private final UserService userService;
    private static final Logger logger = Logger.getLogger(HistoryServiceImpl.class);

    public HistoryServiceImpl(HistoryRepository historyRepository, UserService userService) {
        this.historyRepository = historyRepository;
        this.userService = userService;
    }

    @Override @Transactional
    public List<History> getHistories() {
        logger.debug("Method 'getHistories' started working.");
        return historyRepository.findAll();
    }

    @Override
    public List<History> getHistoriesOfLastNDays(int countMinusDays) {
        logger.debug("Method 'getHistoriesOfLastNDays' started working.");

        LocalDateTime finishOfPeriod = LocalDateTime.now();
        LocalDateTime startOfPeriod = finishOfPeriod.minusDays(countMinusDays);
        return historyRepository.findAllBetweenStartAndFinish(startOfPeriod, finishOfPeriod);
    }

    @Override
    public List<History> getHistoriesOfPeriod(Period period) {
        logger.debug("Method 'getHistoriesOfPeriod' started working.");
        return switch(period) {
            case ALL -> getHistories();
            case THIS_DAY -> getHistoriesOfLastNDays(1);
            case THREE_DAYS -> getHistoriesOfLastNDays(3);
            case THIS_WEEK -> getHistoriesOfLastNDays(7);
            case THIS_MONTH -> getHistoriesOfLastNDays(30);
        };
    }

    @Override
    public List<History> filterHistoriesByTaskType(List<History> histories, TaskType taskType, User user) {
        logger.debug("Method 'filterHistoriesByTaskType' started working.");

        return switch(taskType) {
            case TASKS_CREATED_BY_THIS_USER_OR_ASSIGNED_TO_THIS_USER -> Stream.of(
                            getRecordsWhereUserWasAssignedAsExecutor(histories, user),
                            getStartOfWorkOnTaskRecordsByUser(histories, user),
                            getStopOfWorkOnTaskRecordsByUser(histories, user),
                            getDeadlineChangeRecordsForTasksWhereUserIsAssignedAsExecutor(histories, user),
                            getStatusChangeRecordsForTasksWhereUserIsAssignedAsExecutor(histories, user),
                            getStatusChangeRecordsForTasksCreatedByUser(histories, user),
                            getDeadlineChangeRecordsForTasksCreatedByUser(histories, user),
                            getUserExecutorChangeRecordsForTasksCreatedByUser(histories, user))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            case TASKS_ASSIGNED_TO_THIS_USER ->  Stream.of(
                            getRecordsWhereUserWasAssignedAsExecutor(histories, user),
                            getStartOfWorkOnTaskRecordsByUser(histories, user),
                            getStopOfWorkOnTaskRecordsByUser(histories, user),
                            getDeadlineChangeRecordsForTasksWhereUserIsAssignedAsExecutor(histories, user),
                            getStatusChangeRecordsForTasksWhereUserIsAssignedAsExecutor(histories, user))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            case TASKS_CREATED_BY_THIS_USER -> Stream.of(
                            getStatusChangeRecordsForTasksCreatedByUser(histories, user),
                            getDeadlineChangeRecordsForTasksCreatedByUser(histories, user),
                            getUserExecutorChangeRecordsForTasksCreatedByUser(histories, user))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        };
    }

    @Override
    public List<History> filterHistoriesByChangedFiled(List<History> histories, UpdatableTaskField changedField) {
        logger.debug("Method 'filterHistoriesByChangedFiled' started working.");

        return switch(changedField) {
            case ALL -> histories;

            case STATUS -> histories.stream()
                    .filter(history -> history.getTaskField() == UpdatableTaskField.STATUS)
                    .collect(Collectors.toList());

            case ACTIVITY_STATUS -> histories.stream()
                    .filter(history -> history.getTaskField() == UpdatableTaskField.ACTIVITY_STATUS)
                    .collect(Collectors.toList());

            case DATE_DEADLINE -> histories.stream()
                    .filter(history -> history.getTaskField() == UpdatableTaskField.DATE_DEADLINE)
                    .collect(Collectors.toList());

            case ID_USER_EXECUTOR -> histories.stream()
                    .filter(history -> history.getTaskField() == UpdatableTaskField.ID_USER_EXECUTOR)
                    .collect(Collectors.toList());

            default -> throw new IllegalArgumentException();
        };
    }

    @Override @Transactional
    public List<History> historyOfTask(Task task){
        logger.debug("Method 'historyOfTask' started working.");
        return historyRepository.findByTask(task);
    }

    @Override @Transactional
    public void recordTaskFieldChange(Task task, User userWhoUpdated, UpdatableTaskField field, String oldValue, String newValue){
        logger.debug("Method 'recordTaskFieldChange' started working.");
        historyRepository.save(new History(task, userWhoUpdated, field, oldValue, newValue));
    }

    @Override
    public List<History> fillInFieldsOldAndNewUserExecutors(List<History> originalList) {
        logger.debug("Method 'fillInFieldsOldAndNewUserExecutors' started working.");
        return originalList.stream()
                .map(task ->{
                    if (task.getTaskField() == UpdatableTaskField.ID_USER_EXECUTOR){
                        if(task.getOldValue() != null)
                            task.setOldUserExecutor(userService.getUser(task.getOldValue()));
                        task.setNewUserExecutor(userService.getUser(task.getNewValue()));
                    }
                    return task;
                })
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public List<History> getHistoriesAboutChangesOfActivityStatusOfTaskByUserForPeriod
            (Task task, User user, LocalDateTime periodTimeStart, LocalDateTime periodTimeFinish){

        logger.debug("Method 'getHistoriesAboutChangesOfActivityStatusOfTaskByUserForPeriod' started working.");

        return historyRepository.findByTaskAndUserWhoUpdatedAndTaskFieldIsAndInPeriod
                (task.getId(), user.getId(), periodTimeStart, periodTimeFinish);
    }

    @Override @Transactional
    public List<History> getRecordsWhereUserWasAssignedAsExecutor(List<History> histories, User user){
        logger.debug("Method 'getRecordsWhereUserWasAssignedAsExecutor' started working.");
        return histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.ID_USER_EXECUTOR
                        && history.getNewValue() == String.valueOf(user.getId()))
                .map(history -> {
                    history.setTypeEvent(TypeEvent.ASSIGNED_TASK);
                    return history;
                })
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public List<History> getStartOfWorkOnTaskRecordsByUser(List<History> histories, User user){
        logger.debug("Method 'getStartOfWorkOnTaskRecordsByUser' started working.");
        return histories.stream()
                .filter(history -> Objects.equals(history.getUserWhoUpdated(), user)
                        && history.getTaskField() == UpdatableTaskField.ACTIVITY_STATUS
                        && history.getOldValue().equals("false"))
                .map(history -> {
                    history.setTypeEvent(TypeEvent.STARTED_WORK);
                    return history;
                })
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public List<History> getStopOfWorkOnTaskRecordsByUser(List<History> histories, User user){
        logger.debug("Method 'getStopOfWorkOnTaskRecordsByUser' started working.");
        return histories.stream()
                .filter(history -> Objects.equals(history.getUserWhoUpdated(), user)
                        && history.getTaskField() == UpdatableTaskField.ACTIVITY_STATUS
                        && history.getOldValue().equals("true"))
                .map(history -> {
                    history.setTypeEvent(TypeEvent.STOPPED_WORK);
                    return history;
                })
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public List<History> getDeadlineChangeRecordsForTasksWhereUserIsAssignedAsExecutor(List<History> histories, User user){

        logger.debug("Method 'getDeadlineChangeRecordsForTasksWhereUserIsAssignedAsExecutor' started working.");

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
    public List<History> getStatusChangeRecordsForTasksWhereUserIsAssignedAsExecutor(List<History> histories, User user){

        logger.debug("Method 'getStatusChangeRecordsForTasksWhereUserIsAssignedAsExecutor' started working.");

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
    public List<History> getStatusChangeRecordsForTasksCreatedByUser(List<History> histories, User user){

        logger.debug("Method 'getStatusChangeRecordsForTasksCreatedByUser' started working.");

        return histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.STATUS
                        && history.getTask().getUserCreator().getId() == user.getId())
                .map(history -> {
                    history.setTypeEvent(TypeEvent.CHANGE_STATUS_FOR_TASK_CREATED_BY_USER);
                    return history;
                })
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public List<History> getDeadlineChangeRecordsForTasksCreatedByUser(List<History> histories, User user){

        logger.debug("Method 'getDeadlineChangeRecordsForTasksCreatedByUser' started working.");

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
    public List<History> getUserExecutorChangeRecordsForTasksCreatedByUser(List<History> histories, User user){

        logger.debug("Method 'getUserExecutorChangeRecordsForTasksCreatedByUser' started working.");

        return histories.stream()
                .filter(history -> history.getTaskField() == UpdatableTaskField.ID_USER_EXECUTOR
                        && history.getTask().getUserCreator().getId() == user.getId())
                .map(history -> {
                    history.setTypeEvent(TypeEvent.CHANGE_ASSIGNED_USER_FOR_TASK_CREATED_BY_USER);
                    return history;
                })
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public LocalDateTime getTheEarliestDateOfActivityStatusChangeByUser(User user) {
        logger.debug("Method 'getTheEarliestDateOfActivityStatusChangeByUser' started working.");
        return historyRepository.getTheEarliestDateOfActivityStatusChangeByUser(user.getId());
    }

    @Override @Transactional
    public LocalDateTime getTheLatestDateOfActivityStatusChangeByUser(User user) {
        logger.debug("Method 'getTheLatestDateOfActivityStatusChangeByUser' started working.");
        return historyRepository.getTheLatestDateOfActivityStatusChangeByUser(user.getId());
    }

    @Override @Transactional
    public boolean isPresentRecordWithParams(User user, Task task, LocalDateTime startPeriod, LocalDateTime finishPeriod){
        logger.debug("Method 'isPresentRecordWithParams' in class started working.");
        Long count = historyRepository.getCountRecordsWithParams(task.getId(), user.getId(), startPeriod, finishPeriod);
        return count > 0;
    }

    @Override
    public History checkUserWorkingInPeriod(User user, LocalDateTime startOfPeriod) {
        logger.debug("Method 'checkUserWorkingInPeriod' in class started working.");

        LocalDateTime LastLDTBeforeStartOfPeriod =
                historyRepository.getLDTForLastRecordAboutActivityStatusChangedBeforeTime(user.getId(), startOfPeriod);

        History history = historyRepository.findHistoryByDate(LastLDTBeforeStartOfPeriod);
        return history;
    }
}
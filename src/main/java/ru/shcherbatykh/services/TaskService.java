package ru.shcherbatykh.services;

import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.classes.Status;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {
    @Transactional
    List<Task> getTasks();

    @Transactional
    void addTask(Task task);

    @Transactional
    Task getTask(long id);

    @Transactional
    void deleteTask(long id);

    @Transactional
    List<Task> getTasksCreatedByUser(User user);

    @Transactional
    List<Task> getTasksAssignedToUser(User user);

    @Transactional
    List<Task> getChildTasks(Task task);

    @Transactional
    void deactivateActiveUserTask(User user);

    // All update methods are responsible for writing a row about update to the History table
    // HistoryService is used for this
    @Transactional
    void updateTitle(long id, User userWhoUpdated, String newTitle);

    @Transactional
    void updateDescription(long id, User userWhoUpdated,  String newDescription);

    @Transactional
    void updateStatus(long id, User userWhoUpdated, Status newStatus);

    @Transactional
    void updateDateDeadline(long id, User userWhoUpdated, LocalDateTime newDateDeadline);

    @Transactional
    void updateUserExecutor(long id, User userWhoUpdated, User newUserExecutor);

    @Transactional
    void updateActivityStatus(long id, User userWhoUpdated, boolean newStatus);
}

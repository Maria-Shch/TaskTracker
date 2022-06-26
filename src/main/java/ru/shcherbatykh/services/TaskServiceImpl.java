package ru.shcherbatykh.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.classes.Status;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.repositories.TaskRepository;
import ru.shcherbatykh.classes.UpdatableTaskField;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService{

    private final TaskRepository taskRepository;
    private final HistoryService historyService;

    public TaskServiceImpl(TaskRepository taskRepository, HistoryService historyService) {
        this.taskRepository = taskRepository;
        this.historyService = historyService;
    }

    @Override @Transactional
    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    @Override @Transactional
    public void addTask(Task task) {
        taskRepository.save(task);
    }

    @Override @Transactional
    public Task getTask(long id) {
        return taskRepository.getTaskById(id);
    }

    @Override @Transactional
    public void deleteTask(long id) {
        taskRepository.deleteById(id);
    }

    @Override @Transactional
    public List<Task> getTasksCreatedByUser(User user){
        return user.getTasksCreatedByUser();
    }

    @Override @Transactional
    public List<Task> getTasksAssignedToUser(User user){
        return user.getTasksAssignedToUser();
    }

    @Override @Transactional
    public List<Task> getChildTasks(Task task){
        List<Long> ids = taskRepository.getIdsChildTasks(task.getId());
        return ids.stream()
                .map(id -> this.getTask(id))
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public void deactivateActiveUserTask(User user){
        List<Task> tasks = getTasksAssignedToUser(user);

        Task activeTask = tasks.stream()
                .filter(task -> task.isActivityStatus())
                .findFirst()
                .orElse(null);

        if (activeTask!=null) updateActivityStatus(activeTask.getId(), user,false);
    }

    // All update methods are responsible for writing a row about update to the History table
    // HistoryService is used for this
    @Override @Transactional
    public void updateTitle(long id, User userWhoUpdated, String newTitle){
        Task task = taskRepository.getTaskById(id);

        String oldTitle = task.getTitle();
        historyService.recordTaskFieldChange(task, userWhoUpdated, UpdatableTaskField.TITLE, oldTitle, newTitle);

        task.setTitle(newTitle);
        taskRepository.save(task);
    }

    @Override @Transactional
    public void updateDescription(long id, User userWhoUpdated, String newDescription){
        Task task = taskRepository.getTaskById(id);

        String oldDescription = task.getDescription();
        historyService.recordTaskFieldChange(task, userWhoUpdated, UpdatableTaskField.DESCRIPTION, oldDescription, newDescription);

        task.setDescription(newDescription);
        taskRepository.save(task);
    }

    @Override @Transactional
    public void updateUserExecutor(long id, User userWhoUpdated, User newUserExecutor){
        Task task = taskRepository.getTaskById(id);

        String oldIdUserExecutor = String.valueOf(task.getUserExecutor().getId());
        String newIdUserExecutor = String.valueOf(newUserExecutor.getId());
        historyService.recordTaskFieldChange(task, userWhoUpdated, UpdatableTaskField.ID_USER_EXECUTOR, oldIdUserExecutor, newIdUserExecutor);

        task.setUserExecutor(newUserExecutor);
        taskRepository.save(task);
    }

    @Override @Transactional
    public void updateStatus(long id, User userWhoUpdated, Status newStatus){
        Task task = taskRepository.getTaskById(id);

        String oldStatus = task.getStatus().toString();
        historyService.recordTaskFieldChange(task, userWhoUpdated, UpdatableTaskField.STATUS, oldStatus, newStatus.toString());

        task.setStatus(newStatus);
        taskRepository.save(task);
    }

    @Override @Transactional
    public void updateDateDeadline(long id, User userWhoUpdated, LocalDateTime newDateDeadline){
        Task task = taskRepository.getTaskById(id);

        String oldDateDeadline = task.getDateDeadline().toString();
        historyService.recordTaskFieldChange(task, userWhoUpdated, UpdatableTaskField.DATE_DEADLINE, oldDateDeadline, newDateDeadline.toString());

        task.setDateDeadline(newDateDeadline);
        taskRepository.save(task);
    }

    @Override @Transactional
    public void updateActivityStatus(long id, User userWhoUpdated, boolean newActivityStatus){
        Task task = taskRepository.getTaskById(id);

        String oldActivityStatus = String.valueOf(task.isActivityStatus());
        historyService.recordTaskFieldChange(task, userWhoUpdated, UpdatableTaskField.ACTIVITY_STATUS, oldActivityStatus, String.valueOf(newActivityStatus));

        task.setActivityStatus(newActivityStatus);
        taskRepository.save(task);
    }
}

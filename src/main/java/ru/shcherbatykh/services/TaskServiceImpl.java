package ru.shcherbatykh.services;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.classes.Status;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.repositories.TaskRepository;
import ru.shcherbatykh.classes.UpdatableTaskField;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.shcherbatykh.utils.CommandUtils.sortTasksByStatus;

@Service
public class TaskServiceImpl implements TaskService{

    private final TaskRepository taskRepository;
    private final HistoryService historyService;
    private static final Logger logger = Logger.getLogger(TaskServiceImpl.class);

    public TaskServiceImpl(TaskRepository taskRepository, HistoryService historyService) {
        this.taskRepository = taskRepository;
        this.historyService = historyService;
    }

    @Override @Transactional
    public List<Task> getTasks() {
        logger.debug("Method 'getTasks' started working.");
        return taskRepository.findAll();
    }

    @Override @Transactional
    public void addTask(Task task) {
        logger.debug("Method 'addTask' started working.");
        taskRepository.save(task);
        if(task.getUserExecutor() != null){
            updateUserExecutor(task.getId(), task.getUserCreator(),task.getUserExecutor());
        }
        logger.info("User (id=" + task.getUserCreator().getId() + ") added new task (id=" + task.getId());
    }

    @Override @Transactional
    public Task getTask(long id) {
        logger.debug("Method 'getTask' started working.");
        return taskRepository.getTaskById(id);
    }

    @Override @Transactional
    public List<Task> getAllChildTasks(Task task){
        logger.debug("Method 'getAllChildTasks' started working.");

        List<Long> ids = taskRepository.getIdsChildTasks(task.getId());
        return ids.stream()
                .map(id -> this.getTask(id))
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public List<Task> getFirstChildTasks(Task task) {
        logger.debug("Method 'getFirstChildTasks' started working.");
        return taskRepository.getTasksByParentTask(task);
    }

    @Override @Transactional
    public void deactivateActiveTaskUser(User user){
        logger.debug("Method 'deactivateActiveTaskUser' started working.");

        List<Task> tasks = user.getTasksAssignedToUser();

        Task activeTask = tasks.stream()
                .filter(task -> task.isActivityStatus())
                .findFirst()
                .orElse(null);

        if (activeTask!=null) updateActivityStatus(activeTask.getId(), user,false);
    }

    @Override @Transactional
    public List<Task> getTasksInHierarchicalOrder(){
        logger.debug("Method 'getTasksInHierarchicalOrder' started working.");

        List<Task> allTasks = getTasks();

        List<Task> rootTasks = allTasks.stream()
                .filter(task -> task.getParentTask() == null)
                .collect(Collectors.toList());

        List<Task> mainRootTasks = new ArrayList<>(rootTasks);
        List<Task> resultList = new ArrayList<>();

        sortTasksByStatus(mainRootTasks);

        Deque<Task> deque = new ArrayDeque();
        for (int i = mainRootTasks.size()-1; i >=0 ; i--) {
            deque.push(mainRootTasks.get(i));
        }

        // Depth-First Search of task-tree
        while(deque.size() != 0){
            Task task = deque.pop();
             resultList.add(task);

            if (getFirstChildTasks(task).size()!=0){

                List<Task> allChildTask = getAllChildTasks(task);

                for(Task task_:allChildTask) {
                    int space = task_.getSpace();
                    task_.setSpace(space+1);
                }

                List<Task> firstChildTasks = getFirstChildTasks(task);
                sortTasksByStatus(firstChildTasks);

                for (int i = firstChildTasks.size()-1; i >=0 ; i--) {
                     deque.push(firstChildTasks.get(i));
                }
            }
        }

        return resultList;
    }

    @Override @Transactional
    public List<Task> getTasksForStatisticsAnalysis(String idTask, User user, boolean isTaskWithChildren,
                                                    LocalDateTime startPeriod, LocalDateTime finishPeriod){

        logger.debug("Method 'getTasksForStatisticsAnalysis' started working.");

        List<Task> rootTasks = new ArrayList<>();
        List<Task> resultList = new ArrayList<>();

        if (idTask.equals("all")){
            rootTasks = getTasks().stream()
                    .filter(task -> task.getParentTask() == null)
                    .collect(Collectors.toList());
        }
        else rootTasks.add(getTask(Long.valueOf(idTask)));

        if(isTaskWithChildren){
            resultList.addAll(rootTasks);
            for(Task task : rootTasks){
                resultList.addAll(getAllChildTasks(task));
            }
        }
        else resultList.addAll(rootTasks);

        return filterTasksUserWorkedInPeriod(resultList, user, startPeriod, finishPeriod);
    }

    @Override
    public List<Task> filterTasksUserWorkedInPeriod(List<Task> tasks, User user, LocalDateTime startPeriod,
                                                    LocalDateTime finishPeriod){

        logger.debug("Method 'filterTasksUserWorkedInPeriod' started working.");

        return tasks.stream()
                .filter(task -> historyService.isPresentRecordWithParams(user, task, startPeriod, finishPeriod))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getTasksUserHasEverWorkedOn(User user) {
        logger.debug("Method 'getTasksUserHasEverWorkedOn' started working.");
        List<Long> idTasks = taskRepository.getIdTasksUserHasEverWorkedOn(user.getId());
        return idTasks.stream()
                .map(idTask -> getTask(idTask))
                .collect(Collectors.toList());
    }

    // All update methods are responsible for writing a row about update to the History table
    // HistoryService is used for this
    @Override @Transactional
    public void updateTitle(long idTask, User userWhoUpdated, String newTitle){

        logger.debug("Method 'updateTitle' started working.");

        Task task = taskRepository.getTaskById(idTask);

        String oldTitle = task.getTitle();
        historyService.recordTaskFieldChange(task, userWhoUpdated, UpdatableTaskField.TITLE, oldTitle, newTitle);

        task.setTitle(newTitle);
        taskRepository.save(task);

        logger.info("User (id=" + userWhoUpdated.getId() + ") updated title of task (id=" + idTask + ") from '"
                + oldTitle + "' to '" + newTitle +"'");
    }

    @Override @Transactional
    public void updateDescription(long idTask, User userWhoUpdated, String newDescription){

        logger.debug("Method 'updateDescription' started working.");

        Task task = taskRepository.getTaskById(idTask);

        String oldDescription = task.getDescription();
        historyService.recordTaskFieldChange(task, userWhoUpdated, UpdatableTaskField.DESCRIPTION, oldDescription, newDescription);

        task.setDescription(newDescription);
        taskRepository.save(task);

        logger.info("User (id=" + userWhoUpdated.getId() + ") updated description of task (id=" + idTask + ") from '"
                + oldDescription + "' to '" + newDescription +"'");
    }

    @Override @Transactional
    public void updateUserExecutor(long idTask, User userWhoUpdated, User newUserExecutor){

        logger.debug("Method 'updateUserExecutor' started working.");

        Task task = taskRepository.getTaskById(idTask);
        String oldIdUserExecutor = null;
        if(task.getUserExecutor() != null){
            oldIdUserExecutor = String.valueOf(task.getUserExecutor().getId());
        }

        String newIdUserExecutor = String.valueOf(newUserExecutor.getId());
        historyService.recordTaskFieldChange(task, userWhoUpdated, UpdatableTaskField.ID_USER_EXECUTOR, oldIdUserExecutor, newIdUserExecutor);

        task.setUserExecutor(newUserExecutor);
        taskRepository.save(task);

        logger.info("User (id=" + userWhoUpdated.getId() + ") updated user executor of task (id=" + idTask + ") from '"
                + oldIdUserExecutor + "' to '" + newIdUserExecutor +"'");
    }

    @Override @Transactional
    public void updateStatus(long idTask, User userWhoUpdated, Status newStatus){

        logger.debug("Method 'updateStatus' started working.");

        Task task = taskRepository.getTaskById(idTask);

        String oldStatus = task.getStatus().toString();
        historyService.recordTaskFieldChange(task, userWhoUpdated, UpdatableTaskField.STATUS, oldStatus, newStatus.toString());

        task.setStatus(newStatus);
        taskRepository.save(task);

        logger.info("User (id=" + userWhoUpdated.getId() + ") updated status of task (id=" + idTask + ") from '"
                + oldStatus + "' to '" + newStatus +"'");
    }

    @Override @Transactional
    public void updateDateDeadline(long idTask, User userWhoUpdated, LocalDateTime newDateDeadline){

        logger.debug("Method 'updateDateDeadline' started working.");

        Task task = taskRepository.getTaskById(idTask);
        String oldDateDeadline = null;

        if(task.getDateDeadline() != null){
            oldDateDeadline = task.getDateDeadline().toString();
        }

        historyService.recordTaskFieldChange(task, userWhoUpdated, UpdatableTaskField.DATE_DEADLINE, oldDateDeadline, newDateDeadline.toString());

        task.setDateDeadline(newDateDeadline);
        taskRepository.save(task);

        logger.info("User (id=" + userWhoUpdated.getId() + ") updated due date of task (id=" + idTask + ") from '"
                + oldDateDeadline + "' to '" + newDateDeadline +"'");
    }

    @Override @Transactional
    public void updateActivityStatus(long idTask, User userWhoUpdated, boolean newActivityStatus){

        logger.debug("Method 'updateActivityStatus' started working.");

        Task task = taskRepository.getTaskById(idTask);

        String oldActivityStatus = String.valueOf(task.isActivityStatus());
        historyService.recordTaskFieldChange(task, userWhoUpdated, UpdatableTaskField.ACTIVITY_STATUS, oldActivityStatus, String.valueOf(newActivityStatus));

        task.setActivityStatus(newActivityStatus);
        taskRepository.save(task);

        logger.info("User (id=" + userWhoUpdated.getId() + ") updated activity status of task (id=" + idTask + ") from '"
                + oldActivityStatus + "' to '" + newActivityStatus +"'");
    }
}

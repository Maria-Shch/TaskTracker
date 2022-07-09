package ru.shcherbatykh.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.classes.Status;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.repositories.TaskRepository;
import ru.shcherbatykh.classes.UpdatableTaskField;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import static ru.shcherbatykh.utils.CommandUtils.sortTasksByStatus;

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
        if(task.getUserExecutor() != null){
            updateUserExecutor(task.getId(), task.getUserCreator(),task.getUserExecutor());
        }
    }

    @Override @Transactional
    public Task getTask(long id) {
        return taskRepository.getTaskById(id);
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
    public List<Task> getAllChildTasks(Task task){
        List<Long> ids = taskRepository.getIdsChildTasks(task.getId());
        return ids.stream()
                .map(id -> this.getTask(id))
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public List<Task> getFirstChildTasks(Task task) {
        return taskRepository.getTasksByParentTask(task);
    }

    @Override @Transactional
    public void deactivateActiveTaskUser(User user){
        List<Task> tasks = user.getTasksAssignedToUser();

        Task activeTask = tasks.stream()
                .filter(task -> task.isActivityStatus())
                .findFirst()
                .orElse(null);

        if (activeTask!=null) updateActivityStatus(activeTask.getId(), user,false);
    }

    @Override @Transactional
    public List<Task> getTasksInHierarchicalOrder(){
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
        List<Task> rootTasks = new ArrayList<>();
        List<Task> resultList = new ArrayList<>();

        if (idTask.equals("all")){
            System.out.println("Was signed ALL TASKS");
            rootTasks = getTasks().stream()
                    .filter(task -> task.getParentTask() == null)
                    .collect(Collectors.toList());
        }
        else{
            rootTasks.add(getTask(Long.valueOf(idTask)));
            System.out.println("SELECTED TASK " + getTask(Long.valueOf(idTask)));
        }

        System.out.println("rootTasks LIST");
        rootTasks.forEach(System.out::println);

        if(isTaskWithChildren){
            resultList.addAll(rootTasks);
            for(Task task : rootTasks){
                resultList.addAll(getAllChildTasks(task));
            }
        }
        else resultList.addAll(rootTasks);

        System.out.println("RESULT LIST BEFORE FILTER");
        resultList.forEach(System.out::println);

        resultList = filterTasksUserWorkedInPeriod(resultList, user, startPeriod, finishPeriod);

        System.out.println("RESULT LIST AFTER FILTER");
        resultList.forEach(System.out::println);

        return resultList;
    }

    @Override
    public List<Task> filterTasksUserWorkedInPeriod(List<Task> tasks, User user, LocalDateTime startPeriod, LocalDateTime finishPeriod){
        return tasks.stream()
                .filter(task -> historyService.isPresentRecordWithParams(user, task, startPeriod, finishPeriod))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getTasksUserHasEverWorkedOn(User user) {
        List<Long> idTasks = taskRepository.getIdTasksUserHasEverWorkedOn(user.getId());
        return idTasks.stream()
                .map(idTask -> getTask(idTask))
                .collect(Collectors.toList());
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
        String oldIdUserExecutor = null;
        if(task.getUserExecutor() != null){
            oldIdUserExecutor = String.valueOf(task.getUserExecutor().getId());
        }

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
        String oldDateDeadline = null;

        if(task.getDateDeadline() != null){
            oldDateDeadline = task.getDateDeadline().toString();
        }

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

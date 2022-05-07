package ru.shcherbatykh.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.models.History;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticalServiceImpl implements StatisticalService {
    private final TaskService taskService;
    private final HistoryService historyService;

    public StatisticalServiceImpl(TaskService taskService, HistoryService historyService) {
        this.taskService = taskService;
        this.historyService = historyService;
    }

    @Override  @Transactional
    public Long getTimeInMillisSpentByUserOnTaskForPeriod(Task rootTask, User user, LocalDateTime startOfPeriod, LocalDateTime finishOfPeriod){
        List<Task> tasks = taskService.getChildTasks(rootTask);
        tasks.add(rootTask);

        List<History> histories = new ArrayList<>();

        for (Task task: tasks)
            histories.addAll(historyService.getHistoriesAboutChangesOfActivityStatusOfTaskByUserForPeriod(task, user, startOfPeriod, finishOfPeriod));

        histories = histories.stream()
                .sorted(Comparator.comparingLong(History::getId))
                .collect(Collectors.toList());

        List<LocalDateTime> times = histories.stream()
                .map(history -> history.getDate())
                .collect(Collectors.toList());

        if("true".equals(histories.get(0).getOldValue()))
            times.add(0, startOfPeriod);

        if("false".equals(histories.get(histories.size()-1).getOldValue()))
            times.add(finishOfPeriod);

        long resultTime=0L;
        for (int i = 0, j = 1; j < times.size(); i+=2, j+=2)
            resultTime += ChronoUnit.MILLIS.between(times.get(i), times.get(j));

        return resultTime;
    }
}

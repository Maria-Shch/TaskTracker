package ru.shcherbatykh.services;

import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticalService {

    @Transactional
    Long getTimeInMillisSpentByUserOnTaskForPeriod(List<Task> tasks, User user, LocalDateTime startPeriod,
                                                   LocalDateTime finishPeriod, boolean isTaskWithChildren);
}
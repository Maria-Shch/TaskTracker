package ru.shcherbatykh.services;

import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;

import java.time.LocalDateTime;

public interface StatisticalService {

    @Transactional
    Long getTimeInMillisSpentByUserOnTaskForPeriod(Task rootTask, User user,
                                                   LocalDateTime startPeriod, LocalDateTime finishPeriod);
}
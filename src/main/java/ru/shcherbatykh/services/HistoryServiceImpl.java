package ru.shcherbatykh.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.models.History;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;
import ru.shcherbatykh.repositories.HistoryRepository;
import ru.shcherbatykh.classes.UpdatableTaskField;

import java.time.LocalDateTime;
import java.util.List;

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
}
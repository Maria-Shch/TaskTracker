package ru.shcherbatykh.classes;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SortingHistory {
    private Period period;
    private TaskType taskType;
    private UpdatableTaskField changedField;

    public SortingHistory(Period period, TaskType taskType, UpdatableTaskField changedField) {
        this.period = period;
        this.taskType = taskType;
        this.changedField = changedField;
    }
}

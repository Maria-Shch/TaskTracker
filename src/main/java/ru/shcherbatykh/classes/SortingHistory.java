package ru.shcherbatykh.classes;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SortingHistory {
    private Period period;
    private TaskType taskType;
    private UpdatableTaskField changedField;
    private Long idSelectedUser;

    public SortingHistory(Period period, TaskType taskType, UpdatableTaskField changedField, Long idSelectedUser) {
        this.period = period;
        this.taskType = taskType;
        this.changedField = changedField;
        this.idSelectedUser = idSelectedUser;
    }
}

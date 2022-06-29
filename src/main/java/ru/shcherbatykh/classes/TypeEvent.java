package ru.shcherbatykh.classes;

public enum TypeEvent {
    ASSIGNED_TASK,
    STARTED_WORK,
    STOPPED_WORK,
    CHANGE_DEADLINE_FOR_TASK_CREATED_BY_USER,
    CHANGE_STATUS_BY_EXECUTOR,
    CHANGE_STATUS_FOR_TASK_CREATED_BY_USER,
    CHANGE_DEADLINE_BY_EXECUTOR,
    CHANGE_ASSIGNED_USER_FOR_TASK_CREATED_BY_USER;

    @Override
    public String toString() {
        return this.name();
    }
}

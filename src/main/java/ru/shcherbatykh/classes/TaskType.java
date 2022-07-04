package ru.shcherbatykh.classes;

public enum TaskType {
    TASKS_CREATED_BY_THIS_USER_OR_ASSIGNED_TO_THIS_USER("Tasks created by user or assigned to user"),
    TASKS_CREATED_BY_THIS_USER("Tasks created by user"),
    TASKS_ASSIGNED_TO_THIS_USER("Tasks assigned to user");

    private final String displayValue;

    TaskType(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}


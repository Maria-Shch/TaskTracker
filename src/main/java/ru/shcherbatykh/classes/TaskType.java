package ru.shcherbatykh.classes;

public enum TaskType {
    TASKS_CREATED_BY_THIS_USER_OR_ASSIGNED_TO_THIS_USER("Tasks created by me or assigned to me"),
    TASKS_CREATED_BY_THIS_USER("Tasks created by me"),
    TASKS_ASSIGNED_TO_THIS_USER("Tasks assigned to me");

    private final String displayValue;

    TaskType(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}


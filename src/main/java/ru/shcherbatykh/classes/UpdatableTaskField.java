package ru.shcherbatykh.classes;

public enum UpdatableTaskField {
    ALL("All fields"),
    TITLE("Title"),
    DESCRIPTION("Description"),
    ID_USER_EXECUTOR("User executor"),
    STATUS("Status of a task"),
    DATE_DEADLINE("Due date of a task"),
    ACTIVITY_STATUS("Start or stop work on a task");

    private final String displayValue;

    UpdatableTaskField(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}

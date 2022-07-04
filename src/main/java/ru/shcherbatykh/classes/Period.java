package ru.shcherbatykh.classes;

public enum Period {
    ALL("All time"),
    THIS_DAY("This day"),
    THREE_DAYS("Three days"),
    THIS_WEEK("This week"),
    THIS_MONTH("This month");

    private final String displayValue;

    Period(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}

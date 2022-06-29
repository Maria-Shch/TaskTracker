package ru.shcherbatykh.classes;

public enum Period {
    ALL, THIS_DAY, THREE_DAYS, THIS_WEEK, THIS_MONTH;

    @Override
    public String toString() {
        return this.name();
    }
}

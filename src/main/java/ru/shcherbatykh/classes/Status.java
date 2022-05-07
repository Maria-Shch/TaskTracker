package ru.shcherbatykh.classes;

public enum Status {
    TODO, ACCEPTED, IN_PROGRESS, DONE, CANCELED;

    @Override
    public String toString() {
        return this.name();
    }
}
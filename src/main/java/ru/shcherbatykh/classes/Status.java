package ru.shcherbatykh.classes;

// A user with the USER role can usually only do the following: change the status of a task from IN PROGRESS to DONE.
// Also, a user with the USER role can change the status to CANCELED in any moment if he is the CREATOR of this task.
// A user with the ADMIN role can change the status of any task to IN_PROGRESS, DONE, CANCELED at any time
public enum Status {
    // The TO_DO status is assigned to any task automatically when it is created.
    TODO,

    // The task changes from the TO_DO status to the ACCEPTED status when the user clicks the button 'Accept'.
    // This means that the user has seen the task assigned to him and will be planning to work on it.
    ACCEPTED,

    // The IN_PROGRESS status is assigned to the task when
    // the user started working on it for the FIRST TIME (pressed the button 'Activate')
    IN_PROGRESS,

    //The status DONE means that the user has finished working on the task
    DONE,

    //The CANCELED status means that this task has been canceled.
    CANCELED;

    @Override
    public String toString() {
        return this.name();
    }
}
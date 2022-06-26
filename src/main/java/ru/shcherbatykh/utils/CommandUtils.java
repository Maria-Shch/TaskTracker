package ru.shcherbatykh.utils;

import ru.shcherbatykh.models.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class CommandUtils {
    public static String convertPeriodOfTimeToString(long milliseconds){
        Duration duration = Duration.ofMillis(milliseconds);
        long HH = duration.toHours();
        long MM = duration.toMinutesPart();
        long SS = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", HH, MM, SS);
    }

    public static String convertDateToStringForPrint(LocalDateTime localDateTime){
        if (localDateTime == null) return "";
        int DD = localDateTime.getDayOfMonth();
        int MM = localDateTime.getMonthValue();
        int YY = localDateTime.getYear();
        return String.format("%02d.%02d.%02d", DD, MM, YY);
    }

    public static String convertDateAndTimeToStringForPrint(LocalDateTime localDateTime){
        int DD = localDateTime.getDayOfMonth();
        int MM = localDateTime.getMonthValue();
        int YY = localDateTime.getYear();

        int HH = localDateTime.getHour();
        int MN = localDateTime.getMinute();
        return String.format("%02d.%02d.%02d %02d:%02d", DD, MM, YY, HH, MN);
    }

    public static void sortTasksByStatus(List<Task> tasks){
        // This comparator sorts the list of tasks in the following way:
        // the first task is the one with the activity status 'true'.
        // Then all tasks with activity status "false" are sorted by ordinal of enum Status
        // If two tasks have the same ordinal are sorted by title
        Collections.sort(tasks, (task1, task2) -> {
            if (task1.isActivityStatus() == true) return -1;
            if (task2.isActivityStatus() == true) return 1;
            if (task1.getStatus() == task2.getStatus()) {
                return task1.getTitle().compareTo(task2.getTitle());
            } else {
                return task1.getStatus().compareTo(task2.getStatus());
            }
        });
    }
}

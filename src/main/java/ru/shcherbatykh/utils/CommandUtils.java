package ru.shcherbatykh.utils;

import java.time.Duration;

public class CommandUtils {
    public static String convertPeriodOfTimeToString(long milliseconds){
        Duration duration = Duration.ofMillis(milliseconds);
        long HH = duration.toHours();
        long MM = duration.toMinutesPart();
        long SS = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", HH, MM, SS);
    }
}

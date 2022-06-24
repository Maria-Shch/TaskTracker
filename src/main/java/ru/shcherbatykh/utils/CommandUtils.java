package ru.shcherbatykh.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class CommandUtils {
    public static String convertPeriodOfTimeToString(long milliseconds){
        Duration duration = Duration.ofMillis(milliseconds);
        long HH = duration.toHours();
        long MM = duration.toMinutesPart();
        long SS = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", HH, MM, SS);
    }

    public static String convertDateToStringForPrint(LocalDateTime localDateTime){
        int DD = localDateTime.getDayOfMonth();
        int MM = localDateTime.getMonthValue();
        int YY = localDateTime.getYear();
        return String.format("%02d.%02d.%02d", DD, MM, YY);
    }
}

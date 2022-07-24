package ru.shcherbatykh.services;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.models.History;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticalServiceImpl implements StatisticalService {

    private final HistoryService historyService;
    private static final Logger logger = Logger.getLogger(StatisticalServiceImpl.class);

    public StatisticalServiceImpl(HistoryService historyService) {
        this.historyService = historyService;
    }

    @Override  @Transactional
    public Long getTimeInMillisSpentByUserOnTaskForPeriod(List<Task> tasks, User user, LocalDateTime startOfPeriod,
                                                          LocalDateTime finishOfPeriod, boolean isTaskWithChildren){

        logger.debug("Method 'getTimeInMillisSpentByUserOnTaskForPeriod' started working.");

        List<History> histories = new ArrayList<>();

        for (Task task : tasks){
            histories.addAll(historyService.getHistoriesAboutChangesOfActivityStatusOfTaskByUserForPeriod(task, user, startOfPeriod, finishOfPeriod));
        }

        if(histories.size() == 0) {
            System.out.println("histories.size() == 0");
        }

        System.out.println("selected histories");
        for(History h: histories){
            System.out.println(h);
        }

        histories = histories.stream()
                .sorted(Comparator.comparing(History::getDate))
                .collect(Collectors.toList());

        System.out.println("histories was sorting by date");
        System.out.println("histories size = " + histories.size());

        List<LocalDateTime> times = histories.stream()
                .map(history -> history.getDate())
                .collect(Collectors.toList());

        System.out.println("was getting times");
        System.out.println("times size = " + times.size());

        if(histories.size() !=0){
            if ("true".equals(histories.get(0).getOldValue())) {
                times.add(0, startOfPeriod);
                System.out.println("was added time to start :" + startOfPeriod);
            }

            if ("false".equals(histories.get(histories.size() - 1).getOldValue())) {
                times.add(finishOfPeriod);
                System.out.println("was added time to finish :" + finishOfPeriod);
            }
        }

        long resultTime=0L;
        for (int i = 0, j = 1; j < times.size(); i+=2, j+=2)
            resultTime += ChronoUnit.MILLIS.between(times.get(i), times.get(j));

        return resultTime;
    }
}

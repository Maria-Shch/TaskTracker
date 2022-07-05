package ru.shcherbatykh.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.shcherbatykh.models.History;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoryRepository extends CrudRepository<History, Long> {

    List<History> findAll();

    History findHistoryByDate(LocalDateTime date);

    @Query(value =
            "SELECT * FROM history WHERE date BETWEEN :startOfPeriod AND :finishOfPeriod",
            nativeQuery = true)
    List<History> findAllBetweenStartAndFinish(@Param("startOfPeriod") LocalDateTime startOfPeriod,
                                               @Param("finishOfPeriod") LocalDateTime finishOfPeriod);

    List<History> findByTask(Task task);

    @Query(value =
            "SELECT * FROM history WHERE task_id=:idTask AND user_id=:idUser " +
                    "AND task_field='ACTIVITY_STATUS' AND date BETWEEN :startOfPeriod AND :finishOfPeriod",
            nativeQuery = true)
    List<History> findByTaskAndUserWhoUpdatedAndTaskFieldIsAndInPeriod(@Param("idTask") long idTask,
                                                                       @Param("idUser") long idUser,
                                                                       @Param("startOfPeriod") LocalDateTime startOfPeriod,
                                                                       @Param("finishOfPeriod") LocalDateTime finishOfPeriod);

    @Query(value =
            "SELECT MIN (date) FROM history " +
                    "WHERE user_id=:idUser AND task_field='ACTIVITY_STATUS';",
            nativeQuery = true)
    LocalDateTime getTheEarliestDateOfActivityStatusChangeByUser(@Param("idUser") long idUser);

    @Query(value =
            "SELECT MAX (date) FROM history " +
                    "WHERE user_id=:idUser AND task_field='ACTIVITY_STATUS';",
            nativeQuery = true)
    LocalDateTime getTheLatestDateOfActivityStatusChangeByUser(@Param("idUser") long idUser);

    @Query(value =
            "SELECT COUNT(h) FROM history h WHERE h.task_id=:idTask AND h.user_id=:idUser " +
                    "AND h.task_field='ACTIVITY_STATUS' AND date BETWEEN :startOfPeriod and :finishOfPeriod",
            nativeQuery = true)
    Long getCountRecordsWithParams(@Param("idTask") long idTask,
                                  @Param("idUser") long idUser,
                                  @Param("startOfPeriod") LocalDateTime startOfPeriod,
                                  @Param("finishOfPeriod") LocalDateTime finishOfPeriod);

    @Query(value =
            "SELECT MAX(date) FROM history " +
                    "WHERE date < :time AND task_field='ACTIVITY_STATUS' AND user_id=:idUser",
            nativeQuery = true)
    LocalDateTime getLDTForLastRecordAboutActivityStatusChangedBeforeTime(@Param("idUser") long idUser,
                                   @Param("time") LocalDateTime startOfPeriod);
}

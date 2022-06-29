package ru.shcherbatykh.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.shcherbatykh.models.History;
import ru.shcherbatykh.models.Task;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoryRepository extends CrudRepository<History, Long> {

    List<History> findAll();

    @Query(value =
            "SELECT * FROM history WHERE date BETWEEN :startOfPeriod AND :finishOfPeriod",
            nativeQuery = true)
    List<History> findAllOfLastThreeDays(@Param("startOfPeriod") LocalDateTime startOfPeriod,
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
}

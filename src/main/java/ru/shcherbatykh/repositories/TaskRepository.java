package ru.shcherbatykh.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.models.User;

import java.util.List;

@Repository
public interface TaskRepository extends CrudRepository<Task, Long> {

    List<Task> findAll();

    Task getTaskById(long id);

    @Query(value =
            "WITH RECURSIVE r AS ( \n" +
            "    SELECT id FROM tasks WHERE parent_task_id = :id \n" +
            "    UNION \n" +
            "    SELECT tasks.id FROM tasks JOIN r ON tasks.parent_task_id = r.id) \n" +
            "SELECT * FROM r;",
            nativeQuery = true)
    List<Long> getIdsChildTasks(@Param("id") long id);

    List<Task> getTasksByParentTask(Task parentTask);

    @Query(value =
            "SELECT task_id FROM history WHERE user_id=:id " +
                    "AND task_field='ACTIVITY_STATUS' " +
                    "GROUP BY task_id;",
            nativeQuery = true)
    List<Long> getIdTasksUserHasEverWorkedOn(@Param("id") long id);
}

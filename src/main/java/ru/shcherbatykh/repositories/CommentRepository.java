package ru.shcherbatykh.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.shcherbatykh.models.Comment;
import ru.shcherbatykh.models.Task;

import java.util.List;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long> {
    List<Comment> findAll();

    List<Comment> findByTask(Task task);
}

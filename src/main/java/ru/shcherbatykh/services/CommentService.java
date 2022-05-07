package ru.shcherbatykh.services;

import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.models.Comment;
import ru.shcherbatykh.models.Task;

import java.util.List;

public interface CommentService {
    @Transactional
    List<Comment> getComments();

    @Transactional
    void addComment(Comment comment);

    @Transactional
    Comment getComment(long id);

    @Transactional
    List<Comment> getCommentsByTask(Task task);

    @Transactional
    void deleteComment(long id);
}

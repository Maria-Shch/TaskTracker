package ru.shcherbatykh.services;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.models.Comment;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.repositories.CommentRepository;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;
    private static final Logger logger = Logger.getLogger(CommentServiceImpl.class);

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override @Transactional
    public void addComment(Comment comment) {
        logger.debug("Method 'addComment' started working.");
        commentRepository.save(comment);
        logger.info("User (id=" + comment.getUser().getId() + ") added new comment (id=" + comment.getId() + ')');
    }

    @Override @Transactional
    public List<Comment> getCommentsByTask(Task task) {
        logger.debug("Method 'getCommentsByTask' in class " + this.getClass().getSimpleName() + " started working.");
        return commentRepository.findByTask(task);
    }
}

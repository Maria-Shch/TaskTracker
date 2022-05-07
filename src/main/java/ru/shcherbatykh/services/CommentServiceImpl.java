package ru.shcherbatykh.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.models.Comment;
import ru.shcherbatykh.models.Task;
import ru.shcherbatykh.repositories.CommentRepository;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override @Transactional
    public List<Comment> getComments() {
        return commentRepository.findAll();
    }

    @Override @Transactional
    public void addComment(Comment comment) {
        commentRepository.save(comment);
    }

    @Override @Transactional
    public Comment getComment(long id) {
        return commentRepository.getCommentById(id);
    }

    @Override @Transactional
    public List<Comment> getCommentsByTask(Task task) {
        return commentRepository.findByTask(task);
    }

    @Override @Transactional
    public void deleteComment(long id) {
        commentRepository.deleteById(id);
    }
}

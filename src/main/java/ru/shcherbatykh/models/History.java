package ru.shcherbatykh.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import ru.shcherbatykh.classes.TypeEvent;
import ru.shcherbatykh.classes.UpdatableTaskField;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "history")
@NoArgsConstructor
public class History {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User userWhoUpdated;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_field")
    private UpdatableTaskField taskField;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @CreationTimestamp
    private LocalDateTime date;


    // This field for sorting histories
    @Transient
    private TypeEvent typeEvent;

    // These fields are filled in when the UserExecutor field was updated
    @Transient
    private User oldUserExecutor;

    @Transient
    private User newUserExecutor;

    public History(Task task, User userWhoUpdated, UpdatableTaskField taskField, String oldValue, String newValue) {
        this.task = task;
        this.userWhoUpdated = userWhoUpdated;
        this.taskField = taskField;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}

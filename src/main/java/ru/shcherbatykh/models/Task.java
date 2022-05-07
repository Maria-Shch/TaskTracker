package ru.shcherbatykh.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import ru.shcherbatykh.classes.Status;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tasks")
@NoArgsConstructor
public class Task{
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String title;
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_creator_id")
    private User userCreator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_executor_id")
    private User userExecutor;

    @Column(name = "date_creation")
    @CreationTimestamp
    private LocalDateTime dateCreation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.TODO;

//    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinColumn(name = "status_id")
//    private Status status = new Status("To Do");

    @Column(name = "date_deadline")
    @CreationTimestamp
    private LocalDateTime dateDeadline;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @Column(name = "activity_status")
    private boolean activityStatus = false;

    public Task(String title, String description, User userCreator, User userExecutor,
                LocalDateTime dateDeadline, Task parentTask) {
        this.title = title;
        this.description = description;
        this.userCreator = userCreator;
        this.userExecutor = userExecutor;
        this.dateDeadline = dateDeadline;
        this.parentTask = parentTask;
    }
}

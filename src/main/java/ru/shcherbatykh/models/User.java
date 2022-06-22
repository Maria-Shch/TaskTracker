package ru.shcherbatykh.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.transaction.annotation.Transactional;
import ru.shcherbatykh.classes.Role;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private String lastname;
    private String username;
    private String password;
    @Transient
    private String confirmPassword;
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userCreator")
    @ToString.Exclude
    private List<Task> tasksCreatedByUser = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userExecutor")
    @ToString.Exclude
    private List<Task> tasksAssignedToUser = new ArrayList<>();

    public User(String name, String lastname, String username, String password) {
        this.name = name;
        this.lastname = lastname;
        this.username = username;
        this.password = password;
    }
}

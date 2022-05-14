package ru.shcherbatykh.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
    private String login;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userCreator")
    @ToString.Exclude
    private List<Task> tasksCreatedByUser = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userExecutor")
    @ToString.Exclude
    private List<Task> tasksAssignedToUser = new ArrayList<>();

    public User(String name, String lastname, String login, String password, Role role) {
        this.name = name;
        this.lastname = lastname;
        this.login = login;
        this.password = password;
        this.role = role;
    }
}

package ru.shcherbatykh.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.shcherbatykh.classes.Role;
import ru.shcherbatykh.models.User;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    List<User> findAll();

    User getUserById(long id);

    void deleteById(long id);

    List<User> findByRole(Role role);
}

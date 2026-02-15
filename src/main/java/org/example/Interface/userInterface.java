package org.example.Interface;

import org.example.Models.User;

import java.util.List;
import java.util.Optional;

public interface userInterface {
    User create(User user);
    Optional<User> findById(int id);
    List<User> findAll();
    User update(User user);
    boolean delete(int id);

    Optional<User> findByEmail(String email);
}

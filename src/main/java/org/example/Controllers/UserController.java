package org.example.Controllers;

import org.example.Models.User;
import org.example.Services.userService;

import java.util.List;
import java.util.Optional;

public class UserController {

    private final userService userService = new userService();

    public User create(User user) {
        return userService.create(user);
    }

    public Optional<User> findById(int id) {
        return userService.findById(id);
    }

    public List<User> findAll() {
        return userService.findAll();
    }

    public User update(User user) {
        return userService.update(user);
    }

    public boolean delete(int id) {
        return userService.delete(id);
    }

    public Optional<User> findByEmail(String email) {
        return userService.findByEmail(email);
    }
}

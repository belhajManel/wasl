package com.mzo.wasl.service;

import com.mzo.wasl.model.User;

import java.util.Optional;

public interface UserService {
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    Optional<User> getUserByEmail(String email);
    void addUser(User user);
}

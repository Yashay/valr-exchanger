package org.valr.repository;

import org.valr.model.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public void save(User user) {
        if (user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("User or username cannot be null");
        }
        users.put(user.getUsername(), user);
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }
}
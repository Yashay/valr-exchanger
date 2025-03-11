package org.valr.service;

import com.google.inject.Inject;
import org.valr.model.User;
import org.valr.registry.ServiceRegistry;
import org.valr.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

public class UserService {

    private final UserRepository userRepository;

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean registerUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            return false;
        }
        userRepository.save(new User(UUID.randomUUID().toString(), username, password));
        return true;
    }

    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsername(username);
    }
}
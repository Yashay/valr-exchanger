package org.valr.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.valr.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
    }

    @Test
    void testSaveUserValidUser() {
        User user = new User("userId123", "user123", "password");

        userRepository.save(user);

        Optional<User> retrievedUser = userRepository.findByUsername("user123");
        assertTrue(retrievedUser.isPresent());
        assertEquals(user.getUsername(), retrievedUser.get().getUsername());
        assertEquals(user.getUserId(), retrievedUser.get().getUserId());
    }

    @Test
    void testSaveUserNullUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            userRepository.save(null);
        });
    }

    @Test
    void testSaveUserNullUsername() {
        User user = new User("userId123", null, "password");
        assertThrows(IllegalArgumentException.class, () -> {
            userRepository.save(user);
        });
    }

    @Test
    void testFindByUsernameUserNotFound() {
        Optional<User> retrievedUser = userRepository.findByUsername("nonExistentUser");
        assertFalse(retrievedUser.isPresent());
    }

    @Test
    void testFindByUsernameUserFound() {
        User user = new User("userId123", "user123", "password");
        userRepository.save(user);

        Optional<User> retrievedUser = userRepository.findByUsername("user123");
        assertTrue(retrievedUser.isPresent());
        assertEquals(user.getUsername(), retrievedUser.get().getUsername());
        assertEquals(user.getUserId(), retrievedUser.get().getUserId());
    }
}

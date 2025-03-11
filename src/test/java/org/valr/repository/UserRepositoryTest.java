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
    void testSaveAndFindByUsername() {
        User user = new User("1", "testuser", "password123");

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername("testuser");
        assertTrue(foundUser.isPresent());
        assertEquals(user, foundUser.get());
    }

    @Test
    void testFindByUsernameWhenUserDoesNotExist() {
        Optional<User> foundUser = userRepository.findByUsername("nonexistentuser");

        assertFalse(foundUser.isPresent());
    }

    @Test
    void testSaveAndOverwriteUser() {
        User user1 = new User("1", "testuser", "password123");
        User user2 = new User("2", "testuser", "newpassword123");

        userRepository.save(user1);
        userRepository.save(user2);

        Optional<User> foundUser = userRepository.findByUsername("testuser");
        assertTrue(foundUser.isPresent());
        assertEquals(user2, foundUser.get());
    }

    @Test
    void testSaveNullUser() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.save(null);
        });

        assertEquals("User or username cannot be null", exception.getMessage());
    }
}

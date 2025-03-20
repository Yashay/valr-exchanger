package org.valr.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.valr.model.User;
import org.valr.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void testRegisterUser_ShouldRegisterSuccessfully_WhenUsernameIsUnique() {
        String username = "newUser";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = userService.registerUser(username, password);

        assertTrue(result);
        verify(userRepository).save(Mockito.argThat(user ->
                user.getUsername().equals(username) && user.getPassword().equals(password)
        ));
    }

    @Test
    void testRegisterUser_ShouldFail_WhenUsernameAlreadyExists() {
        String username = "existingUser";
        String password = "password123";
        User existingUser = new User(UUID.randomUUID().toString(), username, "anotherPassword");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        boolean result = userService.registerUser(username, password);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAuthenticate_ShouldReturnUser_WhenCredentialsAreCorrect() {
        String username = "validUser";
        String password = "correctPassword";
        User user = new User(UUID.randomUUID().toString(), username, password);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Optional<User> authenticatedUser = userService.authenticate(username, password);

        assertTrue(authenticatedUser.isPresent());
        assertEquals(username, authenticatedUser.get().getUsername());
    }

    @Test
    void testAuthenticate_ShouldReturnEmpty_WhenPasswordIsIncorrect() {
        String username = "validUser";
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        User user = new User(UUID.randomUUID().toString(), username, correctPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Optional<User> authenticatedUser = userService.authenticate(username, wrongPassword);

        assertFalse(authenticatedUser.isPresent());
    }

    @Test
    void testAuthenticate_ShouldReturnEmpty_WhenUsernameDoesNotExist() {
        String username = "nonExistentUser";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<User> authenticatedUser = userService.authenticate(username, password);

        assertFalse(authenticatedUser.isPresent());
    }
}

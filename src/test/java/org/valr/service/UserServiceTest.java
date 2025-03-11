package org.valr.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.valr.model.User;
import org.valr.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void testRegisterUser_Success() {
        String username = "testuser";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = userService.registerUser(username, password);

        assertTrue(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegisterUser_UsernameAlreadyExists() {
        String username = "testuser";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User("1", username, password)));

        boolean result = userService.registerUser(username, password);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testAuthenticate_UserExists() {
        String username = "testuser";
        String password = "password123";
        User user = new User("1", username, password);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Optional<User> result = userService.authenticate(username, password);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    public void testAuthenticate_UserNotFound() {
        String username = "nonexistentuser";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<User> result = userService.authenticate(username, password);

        assertFalse(result.isPresent());
    }
}

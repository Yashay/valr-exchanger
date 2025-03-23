package org.valr.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateUserWithValidData() {
        User user = new User("testUser", "securePassword");

        assertNotNull(user.getUserId());
        assertEquals("testUser", user.getUsername());
        assertEquals("securePassword", user.getPassword());
    }

    @Test
    void shouldGenerateUniqueUserIds() {
        User user1 = new User("userOne", "password1");
        User user2 = new User("userTwo", "password2");

        assertNotEquals(user1.getUserId(), user2.getUserId());
    }

    @Test
    void shouldFailValidationForNullUsername() {
        User user = new User(null, "password");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Username is required")));
    }

    @Test
    void shouldFailValidationForBlankUsername() {
        User user = new User("  ", "password");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Username cannot be blank")));
    }

    @Test
    void shouldFailValidationForNullPassword() {
        User user = new User("username", null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Password is required")));
    }

    @Test
    void shouldFailValidationForBlankPassword() {
        User user = new User("username", "  ");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Password cannot be blank")));
    }

    @Test
    void toStringShouldIncludeAllDetails() {
        User user = new User("toStringUser", "password123");
        String result = user.toString();

        assertTrue(result.contains("toStringUser"));
        assertTrue(result.contains("password123"));
        assertTrue(result.contains("userId="));
    }
}

package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserTest extends AbstractValidationTest<User> {
    @Test
    void testValidUser() {
        User user = User.builder()
                .email("test@example.com")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        assertTrue(validator.validate(user).isEmpty());
    }

    @Test
    void testEmptyEmail() {
        User user = User.builder()
                .email(" ")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Map<String, String> errors = violationsToMap(validator.validate(user));
        assertFalse(errors.isEmpty());
        assertEquals("Email должен быть в правильном формате", errors.get("email"));
    }

    @Test
    void testInvalidEmailFormat() {
        User user = User.builder()
                .email("invalid-email")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Map<String, String> errors = violationsToMap(validator.validate(user));
        assertFalse(errors.isEmpty());
        assertEquals("Email должен быть в правильном формате", errors.get("email"));
    }

    @Test
    void testEmptyLogin() {
        User user = User.builder()
                .email("test@example.com")
                .login(" ")
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Map<String, String> errors = violationsToMap(validator.validate(user));
        assertFalse(errors.isEmpty());
        assertEquals("Логин не может быть пустым и не может содержать пробелы", errors.get("login"));
    }

    @Test
    void testFutureBirthday() {
        User user = User.builder()
                .email("test@example.com")
                .login("login")
                .name("name")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        Map<String, String> errors = violationsToMap(validator.validate(user));
        assertFalse(errors.isEmpty());
        assertEquals("Дата рождения пользователя не может быть в будущем", errors.get("birthday"));
    }

    @Test
    void testNullBirthday() {
        User user = User.builder()
                .email("test@example.com")
                .login("login")
                .name("name")
                .birthday(null)
                .build();

        Map<String, String> errors = violationsToMap(validator.validate(user));
        assertFalse(errors.isEmpty());
        assertEquals("Дата рождения не может быть пустой", errors.get("birthday"));
    }
}
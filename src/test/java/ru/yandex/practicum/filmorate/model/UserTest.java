package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
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

        Map<String, List<String>> errors = violationsToMap(validator.validate(user));
        assertFalse(errors.isEmpty());
        assertTrue(errors.get("email").contains("Email не может быть пустым или содержать пробелы"));
    }

    @Test
    void testInvalidEmailFormat() {
        User user = User.builder()
                .email("invalid-email")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Map<String, List<String>> errors = violationsToMap(validator.validate(user));
        assertFalse(errors.isEmpty());
        assertTrue(errors.get("email").contains("Email должен быть в правильном формате"));
    }

    @Test
    void testInvalidEmailFormatRuText() {
        User user = User.builder()
                .email("тест@сайт.ру")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Map<String, List<String>> errors = violationsToMap(validator.validate(user));
        assertFalse(errors.isEmpty());
        assertTrue(errors.get("email").contains("Email должен содержать только латинские буквы"));
    }

    @Test
    void testEmptyLogin() {
        User user = User.builder()
                .email("test@example.com")
                .login(" ")
                .name("name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Map<String, List<String>> errors = violationsToMap(validator.validate(user));
        assertFalse(errors.isEmpty());
        assertTrue(errors.get("login").contains("Логин не может быть пустым и не может содержать пробелы"));

    }

    @Test
    void testFutureBirthday() {
        User user = User.builder()
                .email("test@example.com")
                .login("login")
                .name("name")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        Map<String, List<String>> errors = violationsToMap(validator.validate(user));
        assertFalse(errors.isEmpty());
        assertTrue(errors.get("birthday").contains("Дата рождения пользователя не может быть в будущем"));
    }

    @Test
    void testNullBirthday() {
        User user = User.builder()
                .email("test@example.com")
                .login("login")
                .name("name")
                .birthday(null)
                .build();

        Map<String, List<String>> errors = violationsToMap(validator.validate(user));
        assertFalse(errors.isEmpty());
        assertTrue(errors.get("birthday").contains("Дата рождения не может быть пустой"));
    }
}
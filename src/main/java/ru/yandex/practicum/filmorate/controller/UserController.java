package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emailToId = new HashMap<>();
    private long id = 1;

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        if (emailToId.containsKey(user.getEmail())) {
            throw new AlreadyExistsException("Пользователь с email " + user.getEmail() + " уже существует");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(id++);
        users.put(user.getId(), user);
        emailToId.put(user.getEmail(), user.getId());
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (users.containsKey(user.getId())) {
            User existingUser = users.get(user.getId());
            if (!existingUser.getEmail().equals(user.getEmail())) {
                emailToId.remove(existingUser.getEmail());
            }
            users.put(user.getId(), user);
            emailToId.put(user.getEmail(), user.getId());
            log.info("Обновлен пользователь: {}", user);
            return user;
        } else {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
    }

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    @GetMapping("/login")
    public User login(@RequestParam String email) {
        return Optional.ofNullable(emailToId.get(email))
                .map(users::get)
                .orElseThrow(() -> new NotFoundException("Пользователь с email " + email + " не найден"));
    }

}

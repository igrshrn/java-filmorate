package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emailToId = new HashMap<>();
    private long id = 1;

    @Override
    public User create(User user) {
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

    @Override
    public User update(User user) {
        if (users.containsKey(user.getId())) {
            User existingUser = users.get(user.getId());
            if (!existingUser.getEmail().equals(user.getEmail())) {
                emailToId.remove(existingUser.getEmail());
            }
            users.put(user.getId(), user);
            emailToId.put(user.getEmail(), user.getId());
            log.info("Обновлен пользователь: {}", user);
        } else {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        return user;
    }

    @Override
    public User getUserById(long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            log.error("Пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public void delete(long id) {
        if (users.containsKey(id)) {
            emailToId.remove(users.get(id).getEmail());
            users.remove(id);
            log.info("Удален пользователь с id: {}", id);
        } else {
            log.error("Пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }
}

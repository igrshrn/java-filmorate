package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserFriendDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        this.getUserById(user.getId());
        return userStorage.update(user);
    }

    public void delete(long id) {
        this.getUserById(id);
        userStorage.delete(id);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User getUserById(long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    public User login(String email) {
        return userStorage.getAll().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь с email " + email + " не найден"));
    }

    public void addFriend(long userId, long friendId) {
        this.getUserById(userId);
        this.getUserById(friendId);
        userStorage.addFriend(userId, friendId);
        log.info("Пользователь с ID {} отправил запрос на дружбу пользователяю с ID {} ", userId, friendId);
    }

    public void confirmFriend(long userId, long friendId) {
        userStorage.confirmFriend(userId, friendId);
        log.info("Пользователь с ID {} подтвердил дружбу с пользователем с ID {} ", userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        this.getUserById(userId);
        this.getUserById(friendId);

        if (this.checkRelationship(userId, friendId)) {
            userStorage.deleteFriend(userId, friendId);
            log.info("Пользователь с ID {} удалил дружбу с пользователем с ID {} ", userId, friendId);
        }
    }

    public boolean checkRelationship(long userId, long friendId) {
        return userStorage.checkRelationship(userId, friendId);
    }

    public Collection<UserFriendDto> getFriends(long userId) {
        this.getUserById(userId);
        return userStorage.getFriends(userId);
    }

    public Collection<UserFriendDto> getCommonFriends(long userId, long otherId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }
}

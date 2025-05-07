package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.FriendshipStatus;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
        return userStorage.update(user);
    }

    public void delete(long id) {
        userStorage.delete(id);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User getUserById(long id) {
        return userStorage.getUserById(id);
    }

    public User login(String email) {
        return userStorage.getAll().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь с email " + email + " не найден"));
    }

    public void addFriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);
        friend.getFriends().put(userId, FriendshipStatus.UNCONFIRMED);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public void confirmFriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user.getFriends().containsKey(friendId) && friend.getFriends().containsKey(userId)) {
            user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
            friend.getFriends().put(userId, FriendshipStatus.CONFIRMED);

            userStorage.update(user);
            userStorage.update(friend);
        } else {
            throw new NotFoundException("Запрос на дружбу не найден");
        }
    }

    public void deleteFriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public Collection<User> getFriends(long userId) {
        User user = userStorage.getUserById(userId);

        return user.getFriends().keySet().stream()
                .filter(friendId -> user.getFriends().get(friendId) == FriendshipStatus.UNCONFIRMED)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(long userId, long otherId) {
        User user = userStorage.getUserById(userId);
        User otherUser = userStorage.getUserById(otherId);

        Set<Long> commonFriendsIds = new HashSet<>(user.getFriends().keySet());
        commonFriendsIds.retainAll(otherUser.getFriends().keySet());

        return commonFriendsIds.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }
}

package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.dto.UserFriendDto;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User user);

    Optional<User> getUserById(long id);

    Collection<User> getAll();

    void delete(long id);

    void addFriend(long userId, long friendId);

    void confirmFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    Collection<UserFriendDto> getFriends(long userId);

    Collection<UserFriendDto> getCommonFriends(long userId, long otherUserId);

    boolean checkRelationship(long userId, long friendId);
}

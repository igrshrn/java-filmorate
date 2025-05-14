package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.user.UserResultSetExtractor;
import ru.yandex.practicum.filmorate.dto.UserFriendDto;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Repository
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    private static final String USER_COLUMNS = """
            SELECT
            u.id AS user_id,
            u.email AS user_email,
            u.login AS user_login,
            u.name AS user_name,
            u.birthday AS user_birthday,
            f.friend_id as friend_id,
            f.status as status
            """;
    private static final String USER_JOIN = "LEFT JOIN friends f ON u.id = f.user_id";
    private static final String BASE_SELECT = USER_COLUMNS + " FROM users u " + USER_JOIN;
    private static final String FIND_ALL = BASE_SELECT;
    private static final String FIND_BY_ID = BASE_SELECT + " WHERE u.id = ?";
    private static final String INSERT = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String DELETE_USER = "DELETE FROM users WHERE id = ?";
    private static final String INSERT_FRIEND = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
    private static final String UPDATE_FRIEND_STATUS = "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?";
    private static final String DELETE_FRIEND = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";

    private static final String FIND_BY_EMAIL = "SELECT * FROM users WHERE email = ?";
    private static final String FIND_COMMON_FRIENDS = """
            SELECT
                u.id AS user_id,
                u.email AS user_email,
                u.login AS user_login,
                u.name AS user_name,
                u.birthday AS user_birthday
            FROM users u
                INNER JOIN friends f1 ON u.id = f1.friend_id
                INNER JOIN friends f2 ON u.id = f2.friend_id
            WHERE f1.user_id = ? AND f2.user_id = ?
            """;
    private static final String FIND_FRIENDS = """
            SELECT
            u2.id AS friend_id,
            u2.email AS friend_email,
            u2.login AS friend_login,
            u2.name AS friend_name,
            u2.birthday AS friend_birthday
                    FROM
            users u1
            JOIN
            friends f ON u1.id = f.user_id
                    JOIN
            users u2 ON f.friend_id = u2.id
                    WHERE
            u1.id = ?
            """;

    public UserDbStorage(JdbcTemplate jdbc, UserResultSetExtractor extractor) {
        super(jdbc, extractor);
    }

    @Override
    public User create(User user) {
        if (this.existsEmail(user.getEmail())) {
            throw new AlreadyExistsException("Пользователь с email " + user.getEmail() + " уже существует");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        long id = insert(INSERT, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        user.setId(id);
        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    public boolean existsEmail(String email) {
        try {
            User user = jdbc.queryForObject(
                    FIND_BY_EMAIL,
                    (rs, rowNum) -> User.builder().email(rs.getString("email")).build(),
                    email
            );
            return user != null;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public User update(User user) {
        update(UPDATE, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        log.info("Обновлен пользователь: {}", user);
        return user;
    }

    @Override
    public Optional<User> getUserById(long id) {
        return findOne(FIND_BY_ID, id);
    }

    @Override
    public Collection<User> getAll() {
        return findMany(FIND_ALL);
    }

    @Override
    public void delete(long id) {
        update(DELETE_USER, id);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        update(INSERT_FRIEND, userId, friendId, false);
    }

    @Override
    public void confirmFriend(long userId, long friendId) {
        update(UPDATE_FRIEND_STATUS, true, userId, friendId);
    }

    public boolean checkRelationship(long userId, long friendId) {
        String query = "SELECT 1 FROM friends WHERE user_id = ? OR friend_id = ? LIMIT 1";
        List<Map<String, Object>> result = jdbc.queryForList(query, userId, friendId);
        return !result.isEmpty();
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        update(DELETE_FRIEND, userId, friendId);
    }

    @Override
    public Collection<UserFriendDto> getFriends(long userId) {
        List<Map<String, Object>> maps = jdbc.queryForList(FIND_FRIENDS, userId);

        return maps.stream().map(map -> UserFriendDto.builder()
                .id((Long) map.get("friend_id"))
                .email((String) map.get("friend_email"))
                .login((String) map.get("friend_login"))
                .name((String) map.get("friend_name"))
                .build()
        ).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Collection<UserFriendDto> getCommonFriends(long userId, long otherUserId) {
        List<Map<String, Object>> maps = jdbc.queryForList(FIND_COMMON_FRIENDS, userId, otherUserId);

        return maps.stream().map(map -> UserFriendDto.builder()
                .id((Long) map.get("user_id"))
                .email((String) map.get("user_email"))
                .login((String) map.get("user_login"))
                .name((String) map.get("user_name"))
                .build()
        ).collect(Collectors.toCollection(ArrayList::new));
    }
}

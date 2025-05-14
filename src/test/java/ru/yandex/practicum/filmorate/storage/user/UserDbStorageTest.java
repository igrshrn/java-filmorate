package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dal.film.FilmResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.user.UserResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.user.UserRowMapper;
import ru.yandex.practicum.filmorate.dto.UserFriendDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.utils.RandomUtils;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, UserDbStorage.class, FilmResultSetExtractor.class, FilmRowMapper.class, UserResultSetExtractor.class, UserRowMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserDbStorageTest {
    @Autowired
    private UserDbStorage userDbStorage;
    protected RandomUtils randomUtils = new RandomUtils();

    @Test
    void create() {
        User user = randomUtils.getUser();
        User createdUser = userDbStorage.create(user);

        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(createdUser.getLogin()).isEqualTo(user.getLogin());
        assertThat(createdUser.getName()).isEqualTo(user.getName());
    }

    @Test
    void update() {
        User user = userDbStorage.create(randomUtils.getUser());
        user.setName("Updated name");
        user.setLogin("Updated login");

        User updatedUser = userDbStorage.update(user);

        assertThat(updatedUser.getName()).isEqualTo("Updated name");
        assertThat(updatedUser.getLogin()).isEqualTo("Updated login");
    }

    @Test
    void getUserById() {
        User user = userDbStorage.create(randomUtils.getUser());

        Optional<User> optionalUser = userDbStorage.getUserById(user.getId());

        assertThat(optionalUser)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("id", user.getId())
                );
    }

    @Test
    void getAll() {
        userDbStorage.create(randomUtils.getUser());
        userDbStorage.create(randomUtils.getUser());

        Collection<User> allUsers = userDbStorage.getAll();
        assertThat(allUsers.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void delete() {
        User user = userDbStorage.create(randomUtils.getUser());
        userDbStorage.delete(user.getId());

        Optional<User> optionalUser = userDbStorage.getUserById(user.getId());
        assertThat(optionalUser).isNotPresent();
    }

    @Test
    void addFriend() {
        User user1 = userDbStorage.create(randomUtils.getUser());
        User user2 = userDbStorage.create(randomUtils.getUser());

        userDbStorage.addFriend(user1.getId(), user2.getId());

        boolean relationshipExists = userDbStorage.checkRelationship(user1.getId(), user2.getId());
        assertTrue(relationshipExists);
    }

    @Test
    void confirmFriend() {
        User user1 = userDbStorage.create(randomUtils.getUser());
        User user2 = userDbStorage.create(randomUtils.getUser());

        userDbStorage.addFriend(user1.getId(), user2.getId());
        userDbStorage.confirmFriend(user1.getId(), user2.getId());
        Optional<User> userAfterConfirmed = userDbStorage.getUserById(user1.getId());

        assertThat(userAfterConfirmed)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u.getFriends().get(user2.getId())).isEqualTo(true)
                );
    }

    @Test
    void checkRelationship() {
        User user1 = userDbStorage.create(randomUtils.getUser());
        User user2 = userDbStorage.create(randomUtils.getUser());

        userDbStorage.addFriend(user1.getId(), user2.getId());
        userDbStorage.deleteFriend(user1.getId(), user2.getId());

        boolean relationshipExists = userDbStorage.checkRelationship(user1.getId(), user2.getId());
        assertFalse(relationshipExists);
    }

    @Test
    void deleteFriend() {
        User user1 = userDbStorage.create(randomUtils.getUser());
        User user2 = userDbStorage.create(randomUtils.getUser());

        userDbStorage.addFriend(user1.getId(), user2.getId());
        userDbStorage.deleteFriend(user1.getId(), user2.getId());

        boolean relationshipExists = userDbStorage.checkRelationship(user1.getId(), user2.getId());
        assertFalse(relationshipExists);
    }

    @Test
    void getFriends() {
        User user1 = userDbStorage.create(randomUtils.getUser());
        User user2 = userDbStorage.create(randomUtils.getUser());

        userDbStorage.addFriend(user1.getId(), user2.getId());

        Collection<UserFriendDto> friends = userDbStorage.getFriends(user1.getId());
        assertThat(friends.size()).isEqualTo(1);
    }

    @Test
    void getCommonFriends() {
        User user1 = userDbStorage.create(randomUtils.getUser());
        User user2 = userDbStorage.create(randomUtils.getUser());
        User user3 = userDbStorage.create(randomUtils.getUser());

        userDbStorage.addFriend(user1.getId(), user3.getId());
        userDbStorage.addFriend(user2.getId(), user3.getId());

        Collection<UserFriendDto> commonFriends = userDbStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends.size()).isEqualTo(1);
    }
}
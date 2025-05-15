package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.HttpMethodEnum;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTest extends AbstractControllerTest {

    private Map<String, Object> userToMap(User user) {
        return Map.of(
                "email", user.getEmail(),
                "login", user.getLogin(),
                "name", user.getName(),
                "birthday", user.getBirthday().toString()
        );
    }

    @Test
    void testCreateUser() throws Exception {
        User user = randomUtils.getUser();
        String json = createJson(userToMap(user));

        performRequest(HttpMethodEnum.POST, "/users", json)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void testSetLoginToNameIfNameIsBlank() throws Exception {
        User user = randomUtils.getUser();
        user.setName("");
        String json = createJson(userToMap(user));

        performRequest(HttpMethodEnum.POST, "/users", json)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(user.getLogin()));
    }

    @Test
    void testReturnConflictIfEmailAlreadyExists() throws Exception {
        User user = randomUtils.getUser();
        String json = createJson(userToMap(user));

        performRequest(HttpMethodEnum.POST, "/users", json)
                .andExpect(status().isOk());

        performRequest(HttpMethodEnum.POST, "/users", json)
                .andExpect(status().isConflict());
    }

    @Test
    void testUpdateUser() throws Exception {
        User user = randomUtils.getUser();
        String json = createJson(userToMap(user));

        String response = performRequest(HttpMethodEnum.POST, "/users", json)
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        String updateJson = createJson(Map.of(
                "id", id,
                "email", "updated@example.ru",
                "login", "updatedLogin",
                "name", "Updated Name",
                "birthday", "1992-02-02"
        ));
        performRequest(HttpMethodEnum.PUT, "/users", updateJson)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.ru"))
                .andExpect(jsonPath("$.login").value("updatedLogin"));
    }

    @Test
    void testReturnNotFoundIfUserToUpdateNotExists() throws Exception {
        User user = randomUtils.getUser();
        user.setId(100);
        String json = createJson(userToMap(user));

        performRequest(HttpMethodEnum.PUT, "/users", json)
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteUser() throws Exception {
        User user = randomUtils.getUser();
        String json = createJson(userToMap(user));

        String response = performRequest(HttpMethodEnum.POST, "/users", json)
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        performRequest(HttpMethodEnum.DELETE, "/users/{id}", id)
                .andExpect(status().isOk());

        performRequest(HttpMethodEnum.GET, "/users/{id}", id)
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllUsers() throws Exception {
        int count = randomUtils.getRandomNumber(8);

        for (int i = 0; i < count; i++) {
            User user = randomUtils.getUser();
            String json = createJson(userToMap(user));
            performRequest(HttpMethodEnum.POST, "/users", json);
        }
        performRequest(HttpMethodEnum.GET, "/users")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(count)));
    }

    @Test
    void testGetUserById() throws Exception {
        User user = randomUtils.getUser();
        String json = createJson(userToMap(user));

        String response = performRequest(HttpMethodEnum.POST, "/users", json)
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();
        performRequest(HttpMethodEnum.GET, "/users/{id}", id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void testLogin() throws Exception {
        User user = randomUtils.getUser();
        String json = createJson(userToMap(user));
        performRequest(HttpMethodEnum.POST, "/users", json);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("email", user.getEmail());

        performRequest(HttpMethodEnum.GET, "/users/login", params)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void testAddFriend() throws Exception {
        User user1 = randomUtils.getUser();
        User user2 = randomUtils.getUser();
        String response1 = performRequest(HttpMethodEnum.POST, "/users", createJson(userToMap(user1)))
                .andReturn().getResponse().getContentAsString();

        String response2 = performRequest(HttpMethodEnum.POST, "/users", createJson(userToMap(user2)))
                .andReturn().getResponse().getContentAsString();

        long userId1 = objectMapper.readTree(response1).get("id").asLong();
        long userId2 = objectMapper.readTree(response2).get("id").asLong();

        performRequest(HttpMethodEnum.PUT, "/users/{id}/friends/{friendId}", userId1, userId2)
                .andExpect(status().isOk());

        String user1Response = performRequest(HttpMethodEnum.GET, "/users/{id}", userId1)
                .andReturn().getResponse().getContentAsString();

        System.out.println(user1Response);
        List<Long> friends = objectMapper.readValue(user1Response, User.class).getFriends().keySet()
                .stream()
                .toList();

        assertThat(friends).contains(userId2);
    }

    @Test
    void testDeleteFriend() throws Exception {
        User user1 = randomUtils.getUser();
        User user2 = randomUtils.getUser();

        String response1 = performRequest(HttpMethodEnum.POST, "/users", createJson(userToMap(user1)))
                .andReturn().getResponse().getContentAsString();

        String response2 = performRequest(HttpMethodEnum.POST, "/users", createJson(userToMap(user2)))
                .andReturn().getResponse().getContentAsString();

        long userId1 = objectMapper.readTree(response1).get("id").asLong();
        long userId2 = objectMapper.readTree(response2).get("id").asLong();

        performRequest(HttpMethodEnum.PUT, "/users/{id}/friends/{friendId}", userId1, userId2)
                .andExpect(status().isOk());

        performRequest(HttpMethodEnum.DELETE, "/users/{id}/friends/{friendId}", userId1, userId2)
                .andExpect(status().isOk());

        String user1Response = performRequest(HttpMethodEnum.GET, "/users/{id}", userId1)
                .andReturn().getResponse().getContentAsString();

        List<Long> friends = objectMapper.readValue(user1Response, User.class).getFriends().keySet().stream()
                .toList();

        assertThat(friends).doesNotContain(userId2);
    }

    @Test
    void testGetFriends() throws Exception {
        User user1 = randomUtils.getUser();
        User user2 = randomUtils.getUser();
        User user3 = randomUtils.getUser();

        String response1 = performRequest(HttpMethodEnum.POST, "/users", createJson(userToMap(user1)))
                .andReturn().getResponse().getContentAsString();

        String response2 = performRequest(HttpMethodEnum.POST, "/users", createJson(userToMap(user2)))
                .andReturn().getResponse().getContentAsString();

        String response3 = performRequest(HttpMethodEnum.POST, "/users", createJson(userToMap(user3)))
                .andReturn().getResponse().getContentAsString();

        long userId1 = objectMapper.readTree(response1).get("id").asLong();
        long userId2 = objectMapper.readTree(response2).get("id").asLong();
        long userId3 = objectMapper.readTree(response3).get("id").asLong();

        performRequest(HttpMethodEnum.PUT, "/users/{id}/friends/{friendId}", userId1, userId2)
                .andExpect(status().isOk());

        performRequest(HttpMethodEnum.PUT, "/users/{id}/friends/{friendId}", userId1, userId3)
                .andExpect(status().isOk());

        performRequest(HttpMethodEnum.GET, "/users/{id}/friends", userId1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(userId2))
                .andExpect(jsonPath("$[1].id").value(userId3));

    }

    @Test
    void testGetCommonFriends() throws Exception {
        User user1 = randomUtils.getUser();
        User user2 = randomUtils.getUser();
        User user3 = randomUtils.getUser();
        User user4 = randomUtils.getUser();

        String response1 = performRequest(HttpMethodEnum.POST, "/users", createJson(userToMap(user1)))
                .andReturn().getResponse().getContentAsString();

        String response2 = performRequest(HttpMethodEnum.POST, "/users", createJson(userToMap(user2)))
                .andReturn().getResponse().getContentAsString();

        String response3 = performRequest(HttpMethodEnum.POST, "/users", createJson(userToMap(user3)))
                .andReturn().getResponse().getContentAsString();

        String response4 = performRequest(HttpMethodEnum.POST, "/users", createJson(userToMap(user4)))
                .andReturn().getResponse().getContentAsString();

        long userId1 = objectMapper.readTree(response1).get("id").asLong();
        long userId2 = objectMapper.readTree(response2).get("id").asLong();
        long userId3 = objectMapper.readTree(response3).get("id").asLong();
        long userId4 = objectMapper.readTree(response4).get("id").asLong();

        performRequest(HttpMethodEnum.PUT, "/users/{id}/friends/{friendId}", userId1, userId3)
                .andExpect(status().isOk());
        performRequest(HttpMethodEnum.PUT, "/users/{id}/friends/{friendId}", userId1, userId4)
                .andExpect(status().isOk());

        performRequest(HttpMethodEnum.PUT, "/users/{id}/friends/{friendId}", userId2, userId3)
                .andExpect(status().isOk());

        performRequest(HttpMethodEnum.GET, "/users/{id}/friends/common/{otherId}", userId1, userId2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userId3));

    }
}

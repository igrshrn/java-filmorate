package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void testSetLoginToNameIfNameIsBlank() throws Exception {
        User user = randomUtils.getUser();
        user.setName("");
        String json = createJson(userToMap(user));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(user.getLogin()));
    }

    @Test
    void testReturnConflictIfEmailAlreadyExists() throws Exception {
        User user = randomUtils.getUser();
        String json = createJson(userToMap(user));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isOk());

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isConflict());
    }

    @Test
    void testUpdateUser() throws Exception {
        User user = randomUtils.getUser();
        String json = createJson(userToMap(user));

        String response = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        String updateJson = createJson(Map.of(
                "id", id,
                "email", "updated@example.ru",
                "login", "updatedLogin",
                "name", "Updated Name",
                "birthday", "1992-02-02"
        ));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.ru"))
                .andExpect(jsonPath("$.login").value("updatedLogin"));
    }

    @Test
    void testReturnNotFoundIfUserToUpdateNotExists() throws Exception {
        User user = randomUtils.getUser();
        user.setId(100);
        String json = createJson(userToMap(user));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteUser() throws Exception {
        User user = randomUtils.getUser();
        String json = createJson(userToMap(user));

        String response = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllUsers() throws Exception {
        int count = randomUtils.getRandomNumber(8);

        for (int i = 0; i < count; i++) {
            User user = randomUtils.getUser();
            String json = createJson(userToMap(user));
            mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json));
        }

        mockMvc.perform(get("/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(count)));
    }

    @Test
    void testGetUserById() throws Exception {
        User user = randomUtils.getUser();
        String json = createJson(userToMap(user));

        String response = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value(user.getEmail()));

        String test = mockMvc.perform(get("/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse().getContentAsString();
        System.out.println(test);
    }

    @Test
    void testLogin() throws Exception {
        User user = randomUtils.getUser();
        String json = createJson(userToMap(user));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/users/login")
                        .param("email", user.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void testAddFriend() throws Exception {
        User user1 = randomUtils.getUser();
        User user2 = randomUtils.getUser();

        String response1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(userToMap(user1))))
                .andReturn().getResponse().getContentAsString();

        String response2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(userToMap(user2))))
                .andReturn().getResponse().getContentAsString();

        long userId1 = objectMapper.readTree(response1).get("id").asLong();
        long userId2 = objectMapper.readTree(response2).get("id").asLong();

        mockMvc.perform(put("/users/{id}/friends/{friendId}", userId1, userId2))
                .andExpect(status().isOk());

        String user1Response = mockMvc.perform(get("/users/{id}", userId1))
                .andReturn().getResponse().getContentAsString();

        List<Long> friends = objectMapper.readValue(user1Response, User.class).getFriends()
                .stream()
                .toList();

        assertThat(friends).contains(userId2);
    }

    @Test
    void testDeleteFriend() throws Exception {
        User user1 = randomUtils.getUser();
        User user2 = randomUtils.getUser();

        String response1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(userToMap(user1))))
                .andReturn().getResponse().getContentAsString();

        String response2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(userToMap(user2))))
                .andReturn().getResponse().getContentAsString();

        long userId1 = objectMapper.readTree(response1).get("id").asLong();
        long userId2 = objectMapper.readTree(response2).get("id").asLong();

        mockMvc.perform(put("/users/{id}/friends/{friendId}", userId1, userId2))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/users/{id}/friends/{friendId}", userId1, userId2))
                .andExpect(status().isOk());

        String user1Response = mockMvc.perform(get("/users/{id}", userId1))
                .andReturn().getResponse().getContentAsString();

        System.out.println(user1Response);
        List<Long> friends = objectMapper.readValue(user1Response, User.class).getFriends().stream()
                .toList();

        assertThat(friends).doesNotContain(userId2);
    }

    @Test
    void testGetFriends() throws Exception {
        User user1 = randomUtils.getUser();
        User user2 = randomUtils.getUser();
        User user3 = randomUtils.getUser();

        String response1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(userToMap(user1))))
                .andReturn().getResponse().getContentAsString();

        String response2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(userToMap(user2))))
                .andReturn().getResponse().getContentAsString();

        String response3 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(userToMap(user3))))
                .andReturn().getResponse().getContentAsString();

        long userId1 = objectMapper.readTree(response1).get("id").asLong();
        long userId2 = objectMapper.readTree(response2).get("id").asLong();
        long userId3 = objectMapper.readTree(response3).get("id").asLong();

        mockMvc.perform(put("/users/{id}/friends/{friendId}", userId1, userId2))
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/{id}/friends/{friendId}", userId1, userId3))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends", userId1))
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

        String response1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(userToMap(user1))))
                .andReturn().getResponse().getContentAsString();

        String response2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(userToMap(user2))))
                .andReturn().getResponse().getContentAsString();

        String response3 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(userToMap(user3))))
                .andReturn().getResponse().getContentAsString();

        String response4 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(userToMap(user4))))
                .andReturn().getResponse().getContentAsString();

        long userId1 = objectMapper.readTree(response1).get("id").asLong();
        long userId2 = objectMapper.readTree(response2).get("id").asLong();
        long userId3 = objectMapper.readTree(response3).get("id").asLong();
        long userId4 = objectMapper.readTree(response4).get("id").asLong();

        mockMvc.perform(put("/users/{id}/friends/{friendId}", userId1, userId3))
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/{id}/friends/{friendId}", userId2, userId3))
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/{id}/friends/{friendId}", userId1, userId4))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", userId1, userId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userId3));
    }
}

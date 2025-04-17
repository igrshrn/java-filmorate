package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTest extends AbstractControllerTest {

    private Map<String, Object> defaultUserData() {
        return Map.of(
                "email", "test@example.ru",
                "login", "login",
                "name", "name",
                "birthday", "1990-01-01"
        );
    }

    @Test
    void testCreateUser() throws Exception {
        String json = createJson(defaultUserData());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.ru"));
    }

    @Test
    void testSetLoginToNameIfNameIsBlank() throws Exception {
        String json = createJson(Map.of(
                "email", "test@example.ru",
                "login", "login",
                "name", "",
                "birthday", "1990-01-01"
        ));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("login"));
    }

    @Test
    void testReturnConflictIfEmailAlreadyExists() throws Exception {
        String json = createJson(defaultUserData());

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isOk());

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isConflict());
    }

    @Test
    void testUpdateUser() throws Exception {
        String json = createJson(defaultUserData());

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
        String json = createJson(Map.of(
                "id", 100,
                "email", "test@example.ru",
                "login", "login",
                "name", "name",
                "birthday", "1990-01-01"
        ));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }
}

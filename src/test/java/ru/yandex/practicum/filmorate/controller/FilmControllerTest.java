package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FilmControllerTest extends AbstractControllerTest {

    private Map<String, Object> defaultFilmData() {
        return Map.of(
                "name", "Film",
                "releaseDate", "2025-04-17",
                "duration", 100,
                "description", "Description"
        );
    }

    @Test
    public void testCreateFilm() throws Exception {
        String json = createJson(defaultFilmData());

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Film"))
                .andExpect(jsonPath("$.duration").value(100))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    void testFilmAlreadyExistsException() throws Exception {
        String json = createJson(defaultFilmData());

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    void testUpdateFilm() throws Exception {
        String json = createJson(defaultFilmData());

        String response = mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        String updateJson = createJson(Map.of(
                "id", id,
                "name", "Updated Film",
                "releaseDate", "2025-04-10",
                "duration", 110,
                "description", "Updated Description"
        ));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Film"))
                .andExpect(jsonPath("$.duration").value(110))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    void testNotFoundException() throws Exception {
        String json = createJson(Map.of(
                "id", 100,
                "name", "Film",
                "releaseDate", "2025-04-17",
                "duration", 100,
                "description", "Ghost film"
        ));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }
}

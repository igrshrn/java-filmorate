package ru.yandex.practicum.filmorate.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FilmControllerTest extends AbstractControllerTest {

    private Map<String, Object> filmToMap(Film film) {
        return Map.of(
                "name", film.getName(),
                "releaseDate", film.getReleaseDate().toString(),
                "duration", film.getDuration(),
                "description", film.getDescription()
        );
    }

    @Test
    public void testCreateFilm() throws Exception {
        Film film = randomUtils.getFilm();
        String json = createJson(filmToMap(film));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(film.getName()))
                .andExpect(jsonPath("$.duration").value(film.getDuration()))
                .andExpect(jsonPath("$.description").value(film.getDescription()));
    }

    @Test
    void testFilmAlreadyExistsException() throws Exception {
        Film film = randomUtils.getFilm();
        String json = createJson(filmToMap(film));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(filmToMap(film))))
                .andExpect(status().isConflict());
    }

    @Test
    void testUpdateFilm() throws Exception {
        Film film = randomUtils.getFilm();
        String json = createJson(filmToMap(film));

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
        Film film = randomUtils.getFilm();
        film.setId(1);
        String json = createJson(filmToMap(film));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllFilms() throws Exception {
        int count = randomUtils.getRandomNumber(8);

        for (int i = 0; i < count; i++) {
            String json = createJson(filmToMap(randomUtils.getFilm()));
            mockMvc.perform(post("/films")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json));
        }

        mockMvc.perform(get("/films").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(count)));
    }

    @Test
    void testFilmById() throws Exception {
        Film film = randomUtils.getFilm();
        String json = createJson(filmToMap(film));

        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/films/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(film.getName()))
                .andExpect(jsonPath("$.duration").value(film.getDuration()));
    }

    @Test
    void deleteFilm() throws Exception {
        Film film = randomUtils.getFilm();
        String json = createJson(filmToMap(film));

        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/films/{id}", id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddLike() throws Exception {
        Film film = randomUtils.getFilm();
        String json = createJson(filmToMap(film));

        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();

        User user = randomUtils.getUser();
        String jsonUser = createJson(Map.of(
                "email", user.getEmail(),
                "login", user.getLogin(),
                "name", user.getName(),
                "birthday", user.getBirthday().toString()
        ));
        String responseUser = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long filmId = objectMapper.readTree(response).get("id").asLong();
        long userId = objectMapper.readTree(responseUser).get("id").asLong();

        mockMvc.perform(put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());

        String filmResponse = mockMvc.perform(get("/films/{id}", filmId))
                .andReturn().getResponse().getContentAsString();

        List<Integer> likesArray = JsonPath.read(filmResponse, "$.likes");
        List<Long> likes = likesArray.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList());

        assertThat(likes).contains(userId);
    }

    @Test
    void testDeleteLike() throws Exception {
        Film film = randomUtils.getFilm();
        String json = createJson(filmToMap(film));

        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();

        User user = randomUtils.getUser();
        String jsonUser = createJson(Map.of(
                "email", user.getEmail(),
                "login", user.getLogin(),
                "name", user.getName(),
                "birthday", user.getBirthday().toString()
        ));
        String responseUser = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long filmId = objectMapper.readTree(response).get("id").asLong();
        long userId = objectMapper.readTree(responseUser).get("id").asLong();

        mockMvc.perform(put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());

        String filmResponse = mockMvc.perform(get("/films/{id}", filmId))
                .andReturn().getResponse().getContentAsString();

        List<Integer> likesArray = JsonPath.read(filmResponse, "$.likes");
        List<Long> likes = likesArray.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList());

        assertThat(likes).doesNotContain(userId);
    }

    @Test
    void getPopularFilms() throws Exception {
        int count = randomUtils.getRandomNumber(8);
        for (int i = 0; i < count; i++) {
            Film film = randomUtils.getFilm();
            String json = createJson(filmToMap(film));
            mockMvc.perform(post("/films")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json));
        }

        mockMvc.perform(get("/films/popular")
                        .param("count", String.valueOf(count)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(count)));
    }
}

package ru.yandex.practicum.filmorate.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.HttpMethodEnum;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
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
                "description", film.getDescription(),
                "genres", film.getGenres(),
                "mpa", film.getMpa()
        );
    }

    @Test
    public void testCreateFilm() throws Exception {
        Film film = randomUtils.getFilm();
        String json = createJson(filmToMap(film));
        performRequest(HttpMethodEnum.POST, "/films", json)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(film.getName()))
                .andExpect(jsonPath("$.duration").value(film.getDuration()))
                .andExpect(jsonPath("$.description").value(film.getDescription()));
    }

    @Test
    void testUpdateFilm() throws Exception {
        Film film = randomUtils.getFilm();
        String json = createJson(filmToMap(film));

        String response = performRequest(HttpMethodEnum.POST, "/films", json)
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        Mpa mpa = randomUtils.getRandomMpa();
        String updateJson = createJson(Map.of(
                "id", id,
                "name", "Updated Film",
                "releaseDate", "2025-04-10",
                "duration", 110,
                "description", "Updated Description",
                "genres", randomUtils.getRandomGenres(),
                "mpa", mpa
        ));

        performRequest(HttpMethodEnum.PUT, "/films", updateJson)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Film"))
                .andExpect(jsonPath("$.duration").value(110))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.genres").isArray());
    }

    @Test
    void testNotFoundException() throws Exception {
        Film film = randomUtils.getFilm();
        film.setId(1);
        String json = createJson(filmToMap(film));

        performRequest(HttpMethodEnum.PUT, "/films", json)
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllFilms() throws Exception {
        int count = randomUtils.getRandomNumber(8);

        for (int i = 0; i < count; i++) {
            String json = createJson(filmToMap(randomUtils.getFilm()));
            performRequest(HttpMethodEnum.POST, "/films", json);
        }

        performRequest(HttpMethodEnum.GET, "/films")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(count)));
    }

    @Test
    void testFilmById() throws Exception {
        Film film = randomUtils.getFilm();
        String json = createJson(filmToMap(film));

        String response = performRequest(HttpMethodEnum.POST, "/films", json)
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        performRequest(HttpMethodEnum.GET, "/films/{id}", id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(film.getName()))
                .andExpect(jsonPath("$.duration").value(film.getDuration()))
                .andExpect(jsonPath("$.genres").isArray())
                .andExpect(jsonPath("$.mpa.name").value(film.getMpa().getName()));
    }

    @Test
    void deleteFilm() throws Exception {
        Film film = randomUtils.getFilm();
        String json = createJson(filmToMap(film));

        String response = performRequest(HttpMethodEnum.POST, "/films", json)
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        performRequest(HttpMethodEnum.DELETE, "/films/{id}", id)
                .andExpect(status().isOk());

        performRequest(HttpMethodEnum.GET, "/films/{id}", id)
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddLike() throws Exception {
        Film film = randomUtils.getFilm();
        String json = createJson(filmToMap(film));

        String response = performRequest(HttpMethodEnum.POST, "/films", json)
                .andReturn().getResponse().getContentAsString();

        User user = randomUtils.getUser();
        String jsonUser = createJson(Map.of(
                "email", user.getEmail(),
                "login", user.getLogin(),
                "name", user.getName(),
                "birthday", user.getBirthday().toString()
        ));

        String responseUser = performRequest(HttpMethodEnum.POST, "/users", jsonUser)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long filmId = objectMapper.readTree(response).get("id").asLong();
        long userId = objectMapper.readTree(responseUser).get("id").asLong();

        performRequest(HttpMethodEnum.PUT, "/films/{id}/like/{userId}", filmId, userId)
                .andExpect(status().isOk());

        String filmResponse = performRequest(HttpMethodEnum.GET, "/films/{id}", filmId)
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

        String response = performRequest(HttpMethodEnum.POST, "/films", json)
                .andReturn().getResponse().getContentAsString();

        User user = randomUtils.getUser();
        String jsonUser = createJson(Map.of(
                "email", user.getEmail(),
                "login", user.getLogin(),
                "name", user.getName(),
                "birthday", user.getBirthday().toString()
        ));

        String responseUser = performRequest(HttpMethodEnum.POST, "/users", jsonUser)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long filmId = objectMapper.readTree(response).get("id").asLong();
        long userId = objectMapper.readTree(responseUser).get("id").asLong();

        performRequest(HttpMethodEnum.PUT, "/films/{id}/like/{userId}", filmId, userId)
                .andExpect(status().isOk());

        performRequest(HttpMethodEnum.DELETE, "/films/{id}/like/{userId}", filmId, userId)
                .andExpect(status().isOk());

        String filmResponse = performRequest(HttpMethodEnum.GET, "/films/{id}", filmId)
                .andReturn().getResponse().getContentAsString();

        List<Integer> likesArray = JsonPath.read(filmResponse, "$.likes");
        List<Long> likes = likesArray.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList());

        assertThat(likes).doesNotContain(userId);
    }

    @Test
    void getPopularFilms() throws Exception {
        Film film1 = randomUtils.getFilm();
        performRequest(HttpMethodEnum.POST, "/films", createJson(filmToMap(film1)))
                .andExpect(status().isOk());

        Film film2 = randomUtils.getFilm();
        performRequest(HttpMethodEnum.POST, "/films", createJson(filmToMap(film2)))
                .andExpect(status().isOk());

        String content = performRequest(HttpMethodEnum.GET, "/films/popular")
                .andReturn().getResponse().getContentAsString();
        System.out.println(content);

        performRequest(HttpMethodEnum.GET, "/films/popular")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}

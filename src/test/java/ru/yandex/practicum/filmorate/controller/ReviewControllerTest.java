package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.HttpMethodEnum;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReviewControllerTest extends AbstractControllerTest {
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

    private Map<String, Object> userToMap(User user) {
        return Map.of(
                "email", user.getEmail(),
                "login", user.getLogin(),
                "name", user.getName(),
                "birthday", user.getBirthday().toString()
        );
    }

    private Map<String, Object> reviewToMap(Review review) {
        return Map.of(
                "content", review.getContent(),
                "isPositive", review.getIsPositive(),
                "userId", review.getUserId(),
                "filmId", review.getFilmId()
        );
    }

    private long createFilm() throws Exception {
        Film film = randomUtils.getFilm();
        String json = createJson(filmToMap(film));
        String response = performRequest(HttpMethodEnum.POST, "/films", json)
                .andReturn().getResponse().getContentAsString();

        return getIdByresponse(response, "id");
    }

    private long createUser() throws Exception {
        User user = randomUtils.getUser();
        String json = createJson(userToMap(user));
        String response = performRequest(HttpMethodEnum.POST, "/users", json)
                .andReturn().getResponse().getContentAsString();

        return getIdByresponse(response, "id");
    }

    private long getIdByresponse(String response, String column) throws Exception {
        return objectMapper.readTree(response).get(column).asLong();
    }

    @Test
    void create() throws Exception {
        long filmId = createFilm();
        long userId = createUser();

        Review review = randomUtils.getReview(filmId, userId);
        String json = createJson(reviewToMap(review));

        performRequest(HttpMethodEnum.POST, "/reviews", json)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(review.getContent()))
                .andExpect(jsonPath("$.isPositive").value(review.getIsPositive()));
    }

    @Test
    void update() throws Exception {
        long filmId = createFilm();
        long userId = createUser();

        Review review = randomUtils.getReview(filmId, userId);
        String json = createJson(reviewToMap(review));

        String response = performRequest(HttpMethodEnum.POST, "/reviews", json)
                .andReturn().getResponse().getContentAsString();

        long reviewId = getIdByresponse(response, "reviewId");
        long useful = getIdByresponse(response, "useful");

        String updateJson = createJson(Map.of(
                "reviewId", reviewId,
                "content", "Updated Content",
                "isPositive", true,
                "userId", userId,
                "filmId", filmId,
                "useful", useful
        ));

        performRequest(HttpMethodEnum.PUT, "/reviews", updateJson)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated Content"))
                .andExpect(jsonPath("$.isPositive").value(true));
    }

    @Test
    void delete() throws Exception {
        long filmId = createFilm();
        long userId = createUser();

        Review review = randomUtils.getReview(filmId, userId);
        String json = createJson(reviewToMap(review));

        String response = performRequest(HttpMethodEnum.POST, "/reviews", json)
                .andReturn().getResponse().getContentAsString();

        long reviewId = getIdByresponse(response, "reviewId");

        performRequest(HttpMethodEnum.DELETE, "/reviews/{id}", reviewId)
                .andExpect(status().isOk());

        performRequest(HttpMethodEnum.GET, "/reviews/{id}", reviewId)
                .andExpect(status().isNotFound());
    }

    @Test
    void getReviewById() throws Exception {
        long filmId = createFilm();
        long userId = createUser();

        Review review = randomUtils.getReview(filmId, userId);
        String json = createJson(reviewToMap(review));

        String response = performRequest(HttpMethodEnum.POST, "/reviews", json)
                .andReturn().getResponse().getContentAsString();

        long reviewId = getIdByresponse(response, "reviewId");

        performRequest(HttpMethodEnum.GET, "/reviews/{id}", reviewId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(reviewId))
                .andExpect(jsonPath("$.content").value(review.getContent()));
    }

    @Test
    void getReviewsWithFilmId() throws Exception {
        long filmId = createFilm();
        for (int i = 0; i < 5; i++) {
            long userId = createUser();
            Review review = randomUtils.getReview(filmId, userId);
            String json = createJson(reviewToMap(review));
            performRequest(HttpMethodEnum.POST, "/reviews", json);
        }

        performRequest(HttpMethodEnum.GET, "/reviews?filmId=" + filmId + "&count=3")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getReviewsWithoutFilmId() throws Exception {
        for (int i = 0; i < 10; i++) {
            long filmId = createFilm();
            long userId = createUser();
            Review review = randomUtils.getReview(filmId, userId);
            String json = createJson(reviewToMap(review));
            performRequest(HttpMethodEnum.POST, "/reviews", json);
        }

        performRequest(HttpMethodEnum.GET, "/reviews?count=5")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    void addLike() throws Exception {
        long filmId = createFilm();
        long userId = createUser();

        Review review = randomUtils.getReview(filmId, userId);
        String json = createJson(reviewToMap(review));

        String response = performRequest(HttpMethodEnum.POST, "/reviews", json)
                .andReturn().getResponse().getContentAsString();
        long reviewId = getIdByresponse(response, "reviewId");

        performRequest(HttpMethodEnum.PUT, "/reviews/{id}/like/{userId}", reviewId, userId)
                .andExpect(status().isOk());

        performRequest(HttpMethodEnum.GET, "/reviews/{id}", reviewId)
                .andExpect(jsonPath("$.useful").value(1));
    }

    @Test
    void addDislike() throws Exception {
        long filmId = createFilm();
        long userId = createUser();

        Review review = randomUtils.getReview(filmId, userId);
        String json = createJson(reviewToMap(review));

        String response = performRequest(HttpMethodEnum.POST, "/reviews", json)
                .andReturn().getResponse().getContentAsString();
        long reviewId = getIdByresponse(response, "reviewId");

        performRequest(HttpMethodEnum.PUT, "/reviews/{id}/dislike/{userId}", reviewId, userId)
                .andExpect(status().isOk());

        performRequest(HttpMethodEnum.GET, "/reviews/{id}", reviewId)
                .andExpect(jsonPath("$.useful").value(-1));
    }

    @Test
    void removeLike() throws Exception {
        long filmId = createFilm();
        long userId = createUser();

        Review review = randomUtils.getReview(filmId, userId);
        String json = createJson(reviewToMap(review));

        String response = performRequest(HttpMethodEnum.POST, "/reviews", json)
                .andReturn().getResponse().getContentAsString();
        long reviewId = getIdByresponse(response, "reviewId");

        performRequest(HttpMethodEnum.PUT, "/reviews/{id}/like/{userId}", reviewId, userId)
                .andExpect(status().isOk());
        performRequest(HttpMethodEnum.GET, "/reviews/{id}", reviewId)
                .andExpect(jsonPath("$.useful").value(1));

        performRequest(HttpMethodEnum.DELETE, "/reviews/{id}/like/{userId}", reviewId, userId)
                .andExpect(status().isOk());
        performRequest(HttpMethodEnum.GET, "/reviews/{id}", reviewId)
                .andExpect(jsonPath("$.useful").value(0));

    }

    @Test
    void removeDislike() throws Exception {
        long filmId = createFilm();
        long userId = createUser();

        Review review = randomUtils.getReview(filmId, userId);
        String json = createJson(reviewToMap(review));

        String response = performRequest(HttpMethodEnum.POST, "/reviews", json)
                .andReturn().getResponse().getContentAsString();
        long reviewId = getIdByresponse(response, "reviewId");

        performRequest(HttpMethodEnum.PUT, "/reviews/{id}/dislike/{userId}", reviewId, userId)
                .andExpect(status().isOk());
        performRequest(HttpMethodEnum.GET, "/reviews/{id}", reviewId)
                .andExpect(jsonPath("$.useful").value(-1));

        performRequest(HttpMethodEnum.DELETE, "/reviews/{id}/dislike/{userId}", reviewId, userId)
                .andExpect(status().isOk());
        performRequest(HttpMethodEnum.GET, "/reviews/{id}", reviewId)
                .andExpect(jsonPath("$.useful").value(0));
    }
}
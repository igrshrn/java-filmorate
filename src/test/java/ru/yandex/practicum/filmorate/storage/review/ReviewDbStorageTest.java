package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dal.director.DirectorResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.film.FilmDtoResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.film.FilmDtoRowMapper;
import ru.yandex.practicum.filmorate.dal.film.FilmResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.review.ReviewResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.review.ReviewRowMapper;
import ru.yandex.practicum.filmorate.dal.user.UserResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.user.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.utils.RandomUtils;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ReviewDbStorage.class, ReviewResultSetExtractor.class, ReviewRowMapper.class, FilmDbStorage.class, DirectorDbStorage.class, DirectorResultSetExtractor.class, UserDbStorage.class, FilmResultSetExtractor.class, FilmDtoResultSetExtractor.class, FilmDtoRowMapper.class, FilmRowMapper.class, UserResultSetExtractor.class, UserRowMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReviewDbStorageTest {

    @Autowired
    private ReviewDbStorage reviewDbStorage;
    @Autowired
    private FilmDbStorage filmDbStorage;
    @Autowired
    private UserDbStorage userDbStorage;
    protected RandomUtils randomUtils = new RandomUtils();

    @Test
    void create() {
        Film film = filmDbStorage.create(randomUtils.getFilm());
        User user = userDbStorage.create(randomUtils.getUser());
        Review review = randomUtils.getReview(film.getId(), user.getId());
        Review createdReview = reviewDbStorage.create(review);

        assertThat(createdReview.getContent()).isEqualTo(review.getContent());
        assertThat(createdReview.getFilmId()).isEqualTo(review.getFilmId());
        assertThat(createdReview.getUserId()).isEqualTo(review.getUserId());

    }

    @Test
    void update() {
        Film film = filmDbStorage.create(randomUtils.getFilm());
        User user = userDbStorage.create(randomUtils.getUser());
        Review review = reviewDbStorage.create(randomUtils.getReview(film.getId(), user.getId()));
        review.setContent("Updated content");
        Review updatedReview = reviewDbStorage.update(review);
        assertThat(updatedReview.getContent()).isEqualTo("Updated content");
    }

    @Test
    void getReviewById() {
        Film film = filmDbStorage.create(randomUtils.getFilm());
        User user = userDbStorage.create(randomUtils.getUser());
        Review review = reviewDbStorage.create(randomUtils.getReview(film.getId(), user.getId()));
        Optional<Review> optionalReview = reviewDbStorage.getReviewById(review.getReviewId());

        assertThat(optionalReview)
                .isPresent()
                .hasValueSatisfying(r -> {
                    assertThat(r).hasFieldOrPropertyWithValue("reviewId", review.getReviewId());
                    assertThat(r).hasFieldOrPropertyWithValue("content", review.getContent());
                });
    }

    @Test
    void getAll() {
        for (int i = 0; i < 15; i++) {
            Film film = filmDbStorage.create(randomUtils.getFilm());
            User user = userDbStorage.create(randomUtils.getUser());
            reviewDbStorage.create(randomUtils.getReview(film.getId(), user.getId()));
        }
        Collection<Review> reviews = reviewDbStorage.getAll(10);
        assertThat(reviews.size()).isEqualTo(10);
    }

    @Test
    void getReviewsByFilmId() {
        int count = 10;
        Film film = filmDbStorage.create(randomUtils.getFilm());
        for (int i = 0; i < 15; i++) {
            User user = userDbStorage.create(randomUtils.getUser());
            reviewDbStorage.create(randomUtils.getReview(film.getId(), user.getId()));
        }
        Collection<Review> reviews = reviewDbStorage.getReviewsByFilmId(film.getId(), count);
        assertThat(reviews.size()).isEqualTo(count);
    }

    @Test
    void delete() {
        Film film = filmDbStorage.create(randomUtils.getFilm());
        User user = userDbStorage.create(randomUtils.getUser());
        Review review = reviewDbStorage.create(randomUtils.getReview(film.getId(), user.getId()));

        reviewDbStorage.delete(review.getReviewId());

        Optional<Review> deletedReview = reviewDbStorage.getReviewById(review.getReviewId());
        assertThat(deletedReview).isEmpty();
    }

    @Test
    void addLike() {
        Film film = filmDbStorage.create(randomUtils.getFilm());
        User user = userDbStorage.create(randomUtils.getUser());
        Review review = reviewDbStorage.create(randomUtils.getReview(film.getId(), user.getId()));

        reviewDbStorage.addLike(review.getReviewId(), user.getId());

        Optional<Review> updatedReview = reviewDbStorage.getReviewById(review.getReviewId());
        assertThat(updatedReview).isPresent();
        assertThat(updatedReview.get().getUseful()).isEqualTo(1);
    }

    @Test
    void addDislike() {
        Film film = filmDbStorage.create(randomUtils.getFilm());
        User user = userDbStorage.create(randomUtils.getUser());
        Review review = reviewDbStorage.create(randomUtils.getReview(film.getId(), user.getId()));

        reviewDbStorage.addDislike(review.getReviewId(), user.getId());

        Optional<Review> updatedReview = reviewDbStorage.getReviewById(review.getReviewId());
        assertThat(updatedReview).isPresent();
        assertThat(updatedReview.get().getUseful()).isEqualTo(-1);
    }

    @Test
    void removeLike() {
        Film film = filmDbStorage.create(randomUtils.getFilm());
        User user = userDbStorage.create(randomUtils.getUser());
        Review review = reviewDbStorage.create(randomUtils.getReview(film.getId(), user.getId()));

        reviewDbStorage.addLike(review.getReviewId(), user.getId());
        reviewDbStorage.removeLike(review.getReviewId(), user.getId());

        Optional<Review> updatedReview = reviewDbStorage.getReviewById(review.getReviewId());
        assertThat(updatedReview).isPresent();
        assertThat(updatedReview.get().getUseful()).isEqualTo(0);
    }

    @Test
    void removeDislike() {
        Film film = filmDbStorage.create(randomUtils.getFilm());
        User user = userDbStorage.create(randomUtils.getUser());
        Review review = reviewDbStorage.create(randomUtils.getReview(film.getId(), user.getId()));

        reviewDbStorage.addDislike(review.getReviewId(), user.getId());
        reviewDbStorage.removeDislike(review.getReviewId(), user.getId());

        Optional<Review> updatedReview = reviewDbStorage.getReviewById(review.getReviewId());
        assertThat(updatedReview).isPresent();
        assertThat(updatedReview.get().getUseful()).isEqualTo(0);
    }
}
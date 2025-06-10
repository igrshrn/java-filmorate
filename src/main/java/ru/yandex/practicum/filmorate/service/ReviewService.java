package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserService userService;
    private final FilmService filmService;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage, UserService userService, FilmService filmService) {
        this.reviewStorage = reviewStorage;
        this.userService = userService;
        this.filmService = filmService;
    }

    public Review create(Review review) {
        userService.getUserById(review.getUserId());
        filmService.getFilmById(review.getFilmId());
        Review createdReview = reviewStorage.create(review);
        log.info("Создан новый отзыв: {}", createdReview);
        return createdReview;
    }

    public Review update(Review review) {
        getReviewById(review.getReviewId());
        Review updatedReview = reviewStorage.update(review);
        log.info("Отзыв обновлен: {}", updatedReview);
        return updatedReview;
    }

    public Review getReviewById(long id) {
        Review review = reviewStorage.getReviewById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с ID: " + id + " не найден."));
        log.info("Получен отзыв с ID: {}", id);
        return review;
    }

    public Collection<Review> getAll(int limit) {
        Collection<Review> reviews = reviewStorage.getAll(limit);
        log.info("Получены отзывы, количество отзывов: {}", limit);
        return reviews;
    }

    public Collection<Review> getReviewsByFilmId(long filmId, int count) {
        Collection<Review> reviews;
        if (filmId != 0) {
            filmService.getFilmById(filmId);
            reviews = reviewStorage.getReviewsByFilmId(filmId, count);
            log.info("Получены отзывы для фильма с ID: {}. Количество отзывов: {}", filmId, count);
        } else {
            reviews = reviewStorage.getAll(count);
            log.info("Получены отзывы для всех фильмов, количество отзывов: {}", count);
        }

        return reviews;
    }

    public void delete(long id) {
        reviewStorage.delete(id);
        log.info("Удален отзыв с ID: {}", id);
    }

    public void addLike(long reviewId, long userId) {
        userService.getUserById(userId);
        getReviewById(reviewId);
        reviewStorage.addLike(reviewId, userId);
        log.info("Пользователь с ID: {} поставил лайк отзыву с ID: {}", userId, reviewId);
    }

    public void addDislike(long reviewId, long userId) {
        userService.getUserById(userId);
        getReviewById(reviewId);
        reviewStorage.addDislike(reviewId, userId);
        log.info("Пользователь с ID: {} поставил дизлайк отзыву с ID: {}", userId, reviewId);
    }

    public void removeLike(long reviewId, long userId) {
        userService.getUserById(userId);
        getReviewById(reviewId);
        reviewStorage.removeLike(reviewId, userId);
        log.info("Пользователь с ID: {} удалил лайк с отзыва с ID: {}", userId, reviewId);
    }

    public void removeDislike(long reviewId, long userId) {
        userService.getUserById(userId);
        getReviewById(reviewId);
        reviewStorage.removeDislike(reviewId, userId);
        log.info("Пользователь с ID: {} удалил дизлайк с отзыва с ID: {}", userId, reviewId);
    }
}

package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    Optional<Review> getReviewById(long id);

    Collection<Review> getAll(int limit);

    Collection<Review> getReviewsByFilmId(long filmId, int count);

    void delete(long id);

    void addLike(long reviewId, long userId);

    void addDislike(long reviewId, long userId);

    void removeLike(long reviewId, long userId);

    void removeDislike(long reviewId, long userId);
}

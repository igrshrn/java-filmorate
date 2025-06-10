package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Primary
@Slf4j
@Repository
public class ReviewDbStorage extends BaseRepository<Review> implements ReviewStorage {
    private static final String INSERT = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, film_id = ?, useful = ? WHERE review_id = ?";
    private static final String FIND_BY_ID = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String FIND_ALL = "SELECT * FROM reviews LIMIT ?";
    private static final String FIND_BY_FILM_ID = "SELECT * FROM reviews WHERE film_id = ? LIMIT ?";
    private static final String DELETE = "DELETE FROM reviews WHERE review_id = ?";
    private static final String ADD_LIKE = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
    private static final String ADD_DISLIKE = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
    private static final String REMOVE_LIKE = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
    private static final String REMOVE_DISLIKE = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";

    public ReviewDbStorage(JdbcTemplate jdbc, ResultSetExtractor<Map<Long, Review>> extractor) {
        super(jdbc, extractor);
    }

    @Override
    public Review create(Review review) {
        long id = insert(INSERT, review.getContent(), review.isPositive(), review.getUserId(), review.getFilmId(), review.getUseful());
        review.setReviewId(id);
        return review;
    }

    @Override
    public Review update(Review review) {
        System.out.println("update:");
        System.out.println(review);
        update(UPDATE, review.getContent(), review.isPositive(), review.getUserId(), review.getFilmId(), review.getUseful(), review.getReviewId());
        return review;
    }

    @Override
    public Optional<Review> getReviewById(long id) {
        return findOne(FIND_BY_ID, id);
    }

    @Override
    public Collection<Review> getAll(int limit) {
        return findMany(FIND_ALL, limit);
    }

    @Override
    public Collection<Review> getReviewsByFilmId(long filmId, int count) {
        return findMany(FIND_BY_FILM_ID, filmId, count);
    }

    @Override
    public void delete(long id) {
        delete(DELETE, id);
    }

    @Override
    public void addLike(long reviewId, long userId) {
        update(ADD_LIKE, reviewId);
    }

    @Override
    public void addDislike(long reviewId, long userId) {
        update(ADD_DISLIKE, reviewId);
    }

    @Override
    public void removeLike(long reviewId, long userId) {
        update(REMOVE_LIKE, reviewId);
    }

    @Override
    public void removeDislike(long reviewId, long userId) {
        update(REMOVE_DISLIKE, reviewId);
    }
}

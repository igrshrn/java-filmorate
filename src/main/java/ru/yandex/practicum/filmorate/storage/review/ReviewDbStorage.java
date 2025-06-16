package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Primary
@Slf4j
@Repository
public class ReviewDbStorage extends BaseRepository<Review> implements ReviewStorage {
    private static final String INSERT = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
    private static final String FIND_BY_ID = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String FIND_ALL = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ? ";
    private static final String FIND_BY_FILM_ID = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ? ";
    private static final String DELETE = "DELETE FROM reviews WHERE review_id = ?";
    private static final String DELETE_REVIEW_VOTES = "DELETE FROM review_votes WHERE review_id = ?";

    private static final String CHECK_VOTE = "SELECT is_like FROM review_votes WHERE review_id = ? AND user_id = ?";
    private static final String UPDATE_VOTE = "UPDATE review_votes SET is_like = ? WHERE review_id = ? AND user_id = ?";
    private static final String INSERT_VOTE = "INSERT INTO review_votes (review_id, user_id, is_like) VALUES (?, ?, ?)";
    private static final String DELETE_VOTE = "DELETE FROM review_votes WHERE review_id = ? AND user_id = ?";

    public ReviewDbStorage(JdbcTemplate jdbc, ResultSetExtractor<Map<Long, Review>> extractor) {
        super(jdbc, extractor);
    }

    @Override
    public Review create(Review review) {
        long id = insert(INSERT, review.getContent(), review.getIsPositive(), review.getUserId(), review.getFilmId(), review.getUseful());
        review.setReviewId(id);
        return review;
    }

    @Override
    public Review update(Review review) {
        update(UPDATE, review.getContent(), review.getIsPositive(), review.getReviewId());
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
        delete(DELETE_REVIEW_VOTES, id);
        delete(DELETE, id);
    }

    @Override
    public void addLike(long reviewId, long userId) {
        handleVote(reviewId, userId, true);
    }

    @Override
    public void addDislike(long reviewId, long userId) {
        handleVote(reviewId, userId, false);
    }

    @Override
    public void removeLike(long reviewId, long userId) {
        handleVoteRemoval(reviewId, userId, true);
    }

    @Override
    public void removeDislike(long reviewId, long userId) {
        handleVoteRemoval(reviewId, userId, false);
    }

    private void handleVote(long reviewId, long userId, boolean isLike) {
        List<Boolean> existingVotes = jdbc.query(CHECK_VOTE, (rs, rowNum) -> rs.getBoolean("is_like"), reviewId, userId);

        if (!existingVotes.isEmpty()) {
            boolean existingVote = existingVotes.getFirst();
            if (existingVote != isLike) {
                // Если пользователь меняет голос, обновляем счетчик на 2
                updateReviewUseful(reviewId, isLike ? 2 : -2);
            }
            // Обновляем запись о голосовании
            jdbc.update(UPDATE_VOTE, isLike, reviewId, userId);
        } else {
            // Если записи нет, обновляем счетчик на 1
            updateReviewUseful(reviewId, isLike ? 1 : -1);
            // Добавляем новую запись о голосовании
            jdbc.update(INSERT_VOTE, reviewId, userId, isLike);
        }
    }

    private void handleVoteRemoval(long reviewId, long userId, boolean wasLike) {
        // Удаляем запись о голосовании
        jdbc.update(DELETE_VOTE, reviewId, userId);
        // Обновляем счетчик на -1 или +1 в зависимости от типа удаляемого голоса
        updateReviewUseful(reviewId, wasLike ? -1 : 1);
    }

    private void updateReviewUseful(long reviewId, int delta) {
        String updateUsefulQuery = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";
        jdbc.update(updateUsefulQuery, delta, reviewId);
    }
}

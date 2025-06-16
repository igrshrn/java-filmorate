package ru.yandex.practicum.filmorate.dal.review;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ReviewResultSetExtractor implements ResultSetExtractor<Map<Long, Review>> {
    private final ReviewRowMapper mapper;

    public ReviewResultSetExtractor(ReviewRowMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Map<Long, Review> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, Review> reviewMap = new LinkedHashMap<>();

        while (rs.next()) {
            long reviewId = rs.getLong("review_id");
            reviewMap.computeIfAbsent(reviewId, k -> {
                try {
                    return mapper.mapRow(rs, 1);
                } catch (SQLException e) {
                    throw new RuntimeException("Ошибка маппинга", e);
                }
            });
        }
        return reviewMap;
    }
}

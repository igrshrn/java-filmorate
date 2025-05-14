package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.DataIntegrityException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@RequiredArgsConstructor
public abstract class BaseRepository<T> {
    protected final JdbcTemplate jdbc;
    protected final ResultSetExtractor<Map<Long, T>> extractor;

    protected Optional<T> findOne(String query, Object... params) {
        Map<Long, T> result = jdbc.query(query, extractor, params);
        return result.values().stream().findFirst();
    }

    protected List<T> findMany(String query, Object... params) {
        Map<Long, T> result = jdbc.query(query, extractor, params);
        return new ArrayList<>(result.values());
    }

    protected void update(String query, Object... params) {
        int updated = jdbc.update(query, params);
        if (updated == 0) {
            throw new InternalServerException("Не удалось обновить данные: " + query);
        }
    }

    protected boolean delete(String query, Object... params) {
        int deleted = jdbc.update(query, params);
        return deleted > 0;
    }

    protected long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                return ps;
            }, keyHolder);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException("Ошибка целостности данных: " + e.getMessage());
        }

        List<Map<String, Object>> keys = keyHolder.getKeyList();
        if (keys.isEmpty()) {
            throw new InternalServerException("Не удалось получить ID после вставки");
        }

        return ((Number) keys.getFirst().values().iterator().next()).longValue();
    }
}

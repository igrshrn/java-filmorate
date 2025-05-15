package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.genre.GenreResultSetExtractor;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.*;
import java.util.stream.Collectors;

@Primary
@Slf4j
@Repository
public class GenreDbStorage extends BaseRepository<Genre> implements GenreStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, GenreResultSetExtractor extractor) {
        super(jdbc, extractor);
    }

    @Override
    public List<Genre> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Genre> getById(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Optional<List<Genre>> getGenresByIds(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Optional.of(List.of());
        }

        String inClause = ids.stream().map(id -> "?").collect(Collectors.joining(", "));
        String query = "SELECT * FROM genres WHERE id IN (" + inClause + ")";

        Map<Long, Genre> genreMap = jdbc.query(query, extractor, ids.toArray(new Long[0]));

        if (genreMap.size() != ids.size()) {
            return Optional.empty();
        }

        return Optional.of(new ArrayList<>(genreMap.values()));
    }
}

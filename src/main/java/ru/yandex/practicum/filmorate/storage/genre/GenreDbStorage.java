package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.genre.GenreResultSetExtractor;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;
import java.util.Optional;

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
}

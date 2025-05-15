package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mpa.MpaResultSetExtractor;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;
import java.util.Optional;

@Primary
@Slf4j
@Repository
public class MpaDbStorage extends BaseRepository<Mpa> implements MpaStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa WHERE id = ?";

    public MpaDbStorage(JdbcTemplate jdbc, MpaResultSetExtractor extractor) {
        super(jdbc, extractor);
    }

    @Override
    public List<Mpa> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Mpa> getById(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}

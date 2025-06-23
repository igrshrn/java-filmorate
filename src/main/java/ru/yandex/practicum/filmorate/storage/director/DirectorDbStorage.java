package ru.yandex.practicum.filmorate.storage.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.director.DirectorResultSetExtractor;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class DirectorDbStorage extends BaseRepository<Director> implements DirectorStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM directors";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?";
    private static final String DELETE_DIRECTOR = "DELETE FROM directors WHERE id = ?";
    private static final String DELETE_DIRECTOR_FROM_FILM = "DELETE FROM film_director WHERE director_id = ?";
    private static final String INSERT = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE = "UPDATE directors SET name = ? WHERE id = ?";

    public DirectorDbStorage(JdbcTemplate jdbc, DirectorResultSetExtractor extractor) {
        super(jdbc, extractor);
        log.info("FilmResultSetExtractor initialized: {}", extractor != null);
    }

    @Override
    public List<Director> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Director> getDirectorById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public void delete(Long id) {
        delete(DELETE_DIRECTOR_FROM_FILM, id);
        delete(DELETE_DIRECTOR, id);
    }

    @Override
    public Director create(Director director) {
        long id = insert(INSERT, director.getName());
        director.setId(id);
        return director;
    }

    @Override
    public Director update(Director director) {
        update(UPDATE, director.getName(), director.getId());
        return director;
    }
}

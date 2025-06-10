package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.director.DirectorResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.director.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({DirectorController.class, DirectorService.class, DirectorDbStorage.class, DirectorResultSetExtractor.class, DirectorRowMapper.class})
class DirectorControllerTest {

    private final DirectorDbStorage directorDbStorage;
    @Autowired
    private final JdbcTemplate jdbcTemplate;

    private Director director_1;
    private Director director_2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE from directors");
        jdbcTemplate.update("ALTER TABLE directors ALTER COLUMN id RESTART WITH 1");
        director_1 = Director.builder()
                .name("Квентин Тарантино").build();
        director_2 = Director.builder()
                .name("Гай Ричи").build();
    }

    @Test
    void testCreateDirectorsAndGetAll() {
        directorDbStorage.create(director_1);
        directorDbStorage.create(director_2);
        List<Director> allDirectors = directorDbStorage.getAll();

        assertEquals(2, allDirectors.size());
    }

    @Test
    void getByID() {
        directorDbStorage.create(director_1);
        Optional<Director> director = directorDbStorage.getDirectorById(1L);
        if (director.isPresent()) {
            assertEquals(1, director.get().getId());
            assertEquals("Квентин Тарантино", director.get().getName());
        } else {
            assertEquals(1, 2);
        }
    }


    @Test
    void update() {
    }

    @Test
    void delete() {
    }
}
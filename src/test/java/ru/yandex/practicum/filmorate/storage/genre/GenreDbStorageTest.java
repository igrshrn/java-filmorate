package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dal.genre.GenreResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.genre.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class, GenreResultSetExtractor.class, GenreRowMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GenreDbStorageTest {
    @Autowired
    private GenreDbStorage genreDbStorage;

    @Test
    void getAll() {
        List<Genre> genres = genreDbStorage.getAll();
        assertThat(genres.size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void getById() {
        Optional<Genre> genre = genreDbStorage.getById(1);
        assertThat(genre).isPresent()
                .hasValueSatisfying(genre1 -> {
                    assertThat(genre1.getId()).isEqualTo(1);
                    assertThat(genre1.getName()).isEqualTo("Комедия");
                });
    }
}
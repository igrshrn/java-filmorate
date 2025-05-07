package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.enums.Genre;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest extends AbstractValidationTest<Film> {
    @Test
    public void testValidFilm() {
        Film film = Film.builder()
                .name("Film")
                .description("Description")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(120)
                .mpa(MpaRating.G)
                .genres(Set.of(Genre.COMEDY, Genre.ACTION))
                .build();

        assertTrue(validator.validate(film).isEmpty());
    }

    @Test
    void testEmptyName() {
        Film film = Film.builder()
                .name("")
                .description("Description")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(120)
                .build();

        Map<String, List<String>> errors = violationsToMap(validator.validate(film));
        assertFalse(errors.isEmpty());
        assertTrue(errors.get("name").contains("Название фильма не может быть пустым"));
    }

    @Test
    void testTooLongDescription() {
        Film film = Film.builder()
                .name("Film")
                .description("A".repeat(201))
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(120)
                .build();

        Map<String, List<String>> errors = violationsToMap(validator.validate(film));
        assertFalse(errors.isEmpty());
        assertTrue(errors.get("description").contains("Описание фильма не может быть длиннее 200 символов"));
    }

    @Test
    void testInvalidReleaseDate() {
        Film film = Film.builder()
                .name("Film")
                .description("Description")
                .releaseDate(LocalDate.of(1800, 1, 1))
                .duration(120)
                .build();

        Map<String, List<String>> errors = violationsToMap(validator.validate(film));
        assertFalse(errors.isEmpty());
        assertTrue(errors.get("releaseDate").contains("Дата релиза фильма не может быть раньше 28 декабря 1895 года"));
    }

    @Test
    void testNegativeDuration() {
        Film film = Film.builder()
                .name("Film")
                .description("Description")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(-10)
                .build();

        Map<String, List<String>> errors = violationsToMap(validator.validate(film));
        assertFalse(errors.isEmpty());
        assertTrue(errors.get("duration").contains("Продолжительность фильма должна быть положительной"));
    }

    @Test
    void testZeroDuration() {
        Film film = Film.builder()
                .name("Film")
                .description("Description")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(0)
                .build();

        Map<String, List<String>> errors = violationsToMap(validator.validate(film));
        assertFalse(errors.isEmpty());
        assertTrue(errors.get("duration").contains("Продолжительность фильма должна быть положительной"));
    }
}
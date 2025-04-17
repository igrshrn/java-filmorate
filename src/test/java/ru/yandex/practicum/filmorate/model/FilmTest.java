package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest extends AbstractValidationTest<Film> {
    @Test
    public void testValidFilm() {
        Film film = Film.builder()
                .name("Film")
                .description("Description")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(120)
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

        Map<String, String> errors = violationsToMap(validator.validate(film));
        assertFalse(errors.isEmpty());
        assertEquals("Название фильма не может быть пустым", errors.get("name"));
    }

    @Test
    void testTooLongDescription() {
        Film film = Film.builder()
                .name("Film")
                .description("A".repeat(201))
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(120)
                .build();

        Map<String, String> errors = violationsToMap(validator.validate(film));
        assertFalse(errors.isEmpty());
        assertEquals("Описание фильма не может быть длиннее 200 символов", errors.get("description"));
    }

    @Test
    void testInvalidReleaseDate() {
        Film film = Film.builder()
                .name("Film")
                .description("Description")
                .releaseDate(LocalDate.of(1800, 1, 1))
                .duration(120)
                .build();

        Map<String, String> errors = violationsToMap(validator.validate(film));
        assertFalse(errors.isEmpty());
        assertEquals("Дата релиза фильма не может быть раньше 28 декабря 1895 года", errors.get("releaseDate"));
    }

    @Test
    void testNegativeDuration() {
        Film film = Film.builder()
                .name("Film")
                .description("Description")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(-10)
                .build();

        Map<String, String> errors = violationsToMap(validator.validate(film));
        assertFalse(errors.isEmpty());
        assertEquals("Продолжительность фильма должна быть положительной", errors.get("duration"));
    }

    @Test
    void testZeroDuration() {
        Film film = Film.builder()
                .name("Film")
                .description("Description")
                .releaseDate(LocalDate.of(2023, 1, 1))
                .duration(0)
                .build();

        Map<String, String> errors = violationsToMap(validator.validate(film));
        assertFalse(errors.isEmpty());
        assertEquals("Продолжительность фильма должна быть положительной", errors.get("duration"));
    }
}
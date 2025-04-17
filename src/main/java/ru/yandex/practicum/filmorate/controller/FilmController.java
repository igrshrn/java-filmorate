package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.film.FilmAlreadyExistsException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.film.FilmNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private final Map<String, Long> filmNameToId = new HashMap<>();
    private long id = 1;

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        if (filmNameToId.containsKey(film.getName())) {
            log.error("Фильм с названием {} уже существует", film.getName());
            throw new FilmAlreadyExistsException("Фильм с названием " + film.getName() + " уже существует");
        }
        film.setId(id++);
        films.put(film.getId(), film);
        filmNameToId.put(film.getName(), film.getId());
        log.info("Создан фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            Film existingFilm = films.get(film.getId());
            if (!existingFilm.getName().equals(film.getName())) {
                filmNameToId.remove(existingFilm.getName());
            }
            films.put(film.getId(), film);
            filmNameToId.put(film.getName(), film.getId());
            log.info("Обновлен фильм: {}", film);
            return film;
        } else {
            log.error("Фильм с id {} не найден", film.getId());
            throw new FilmNotFoundException("Фильм с id " + film.getId() + " не найден");
        }
    }

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

}

package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Validated
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody Film film) {
        return filmService.update(film);
    }

    @GetMapping
    public Collection<Film> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable @Positive long id) {
        return filmService.getFilmById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable @Positive long id) {
        filmService.delete(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable @Positive long id, @PathVariable @Positive long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable @Positive long id, @PathVariable @Positive long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmDto> getPopularFilms(@RequestParam(defaultValue = "10") @Positive int count) {
        return filmService.getPopularFilms(count);
    }

}

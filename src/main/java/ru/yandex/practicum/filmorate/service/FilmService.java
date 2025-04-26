package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id);
    }

    public void delete(long id) {
        filmStorage.delete(id);
    }

    public void addLike(long filmId, long userId) {
        Film film = getFilmById(filmId);
        userService.getUserById(userId);
        film.getLikes().add(userId);
        filmStorage.update(film);
        log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, filmId);
    }

    public void deleteLike(long filmId, long userId) {
        Film film = getFilmById(filmId);
        userService.getUserById(userId);
        film.getLikes().remove(userId);
        filmStorage.update(film);
        log.info("Пользователь с id {} удалил лайк с фильма с id {}", userId, filmId);
    }

    public Collection<Film> getPopularFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted((film1, film2) -> Integer.compare(film2.getLikes().size(), film1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}

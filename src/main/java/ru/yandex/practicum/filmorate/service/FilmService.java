package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final MpaService mpaService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService, GenreService genreService, MpaService mpaService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.genreService = genreService;
        this.mpaService = mpaService;
    }

    public Film create(Film film) {
        Set<Long> genreIds = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        genreService.getByIds(genreIds);

        mpaService.getById(film.getMpa().getId());
        return filmStorage.create(film);
    }

    public FilmDto update(Film film) {
        this.getFilmById(film.getId());
        Film updatedFilm = filmStorage.update(film);
        log.info("Фильм с id {} обновлен", film.getId());
        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID: " + id + " не найден."));
    }

    public void delete(long id) {
        filmStorage.delete(id);
    }

    public void addLike(long filmId, long userId) {
        getFilmById(filmId);
        userService.getUserById(userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, filmId);
    }

    public void deleteLike(long filmId, long userId) {
        getFilmById(filmId);
        userService.getUserById(userId);
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь с id {} удалил лайк с фильма с id {}", userId, filmId);
    }

    public Collection<FilmDto> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }
}

package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Optional<Film> getFilmById(long id);

    Collection<Film> getAll();

    void delete(long id);

    Collection<FilmDto> getPopularFilms(int count);

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);
}

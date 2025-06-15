package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Optional<Film> getFilmById(long id);

    Collection<Film> getAll();

    void delete(long id);

    Collection<FilmDto> getPopularFilms(int count, Long genreId, Integer year);

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    Collection<Film> getSortedFilm(Long id, String sort);

    Collection<Film> searchFilms(String query, List<String> by);

    Collection<FilmDto> getRecommendedFilms(long id, int limit);

    Collection<FilmDto> getCommonFilms(long userId, long friendId);
}

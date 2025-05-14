package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.AlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Repository
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long id = 1;

    @Override
    public Film create(Film film) {
        if (films.values().stream().anyMatch(f -> f.equals(film))) {
            throw new AlreadyExistsException("Фильм с такими данными уже существует");
        }

        film.setId(id++);
        films.put(film.getId(), film);

        return film;
    }

    @Override
    public Film update(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Обновлен фильм: {}", film);
            return film;
        } else {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
    }

    @Override
    public Optional<Film> getFilmById(long id) {
        if (films.containsKey(id)) {
            return Optional.ofNullable(films.get(id));
        } else {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public void delete(long id) {
        if (films.containsKey(id)) {
            films.remove(id);
        } else {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public Collection<FilmDto> getPopularFilms(int count) {
        return List.of();
    }

    @Override
    public void addLike(long filmId, long userId) {

    }

    @Override
    public void removeLike(long filmId, long userId) {

    }


}

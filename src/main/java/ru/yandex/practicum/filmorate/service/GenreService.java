package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Slf4j
@Service
public class GenreService {
    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public List<Genre> getAll() {
        return genreStorage.getAll();
    }

    public Genre getById(long id) {
        return genreStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с ID: " + id + " не найден."));
    }
}

package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Slf4j
@Service
public class DirectorService {

    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Collection<Director> getAll() {
        return directorStorage.getAll();
    }

    public Director getByID(Long id) {
        return directorStorage.getDirectorById(id).orElseThrow(() -> new NotFoundException("Режиссер с ID: " + id + " не найден"));
    }

    public void delete(Long id) {
        directorStorage.delete(id);
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        this.getByID(director.getId());
        return directorStorage.update(director);
    }
}
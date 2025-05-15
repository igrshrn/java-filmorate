package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Slf4j
@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public List<Mpa> getAll() {
        return mpaStorage.getAll();
    }

    public Mpa getById(long id) {
        return mpaStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("MPA с ID: " + id + " не найден."));

    }
}

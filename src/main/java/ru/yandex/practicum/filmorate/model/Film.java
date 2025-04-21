package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.utils.validator.film.ValidFilm;

import java.time.LocalDate;

@Data
@Builder
@ValidFilm
@EqualsAndHashCode(of = {"name", "description", "releaseDate", "duration"})
public class Film {
    private long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность фильма не может быть null")
    @Positive(message = "Продолжительность фильма должна быть положительной")
    private Integer duration;
}


package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.model.enums.Genre;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;
import ru.yandex.practicum.filmorate.utils.validator.film.ValidFilm;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    @Builder.Default
    private Set<Long> likes = new HashSet<>();

    @NotNull(message = "Жанры не могут быть пустыми")
    private Set<Genre> genres;

    @NotNull(message = "Рейтинг MPA не может быть пустым")
    private MpaRating mpa;

    @JsonCreator
    public Film(
            @JsonProperty("id") long id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("releaseDate") LocalDate releaseDate,
            @JsonProperty("duration") Integer duration,
            @JsonProperty("likes") Set<Long> likes,
            @JsonProperty("genres") Set<Genre> genres,
            @JsonProperty("mpa") MpaRating mpa) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
        this.likes = likes != null ? likes : new HashSet<>();
        this.genres = genres != null ? genres : new HashSet<>();
    }

}


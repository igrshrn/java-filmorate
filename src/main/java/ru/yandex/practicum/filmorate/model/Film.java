package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
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

    @JsonCreator
    public Film(
            @JsonProperty("id") long id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("releaseDate") LocalDate releaseDate,
            @JsonProperty("duration") Integer duration,
            @JsonProperty("likes") Set<Long> likes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.likes = likes != null ? likes : new HashSet<>();
    }

}


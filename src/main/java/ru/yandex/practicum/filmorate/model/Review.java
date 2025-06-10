package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review {
    private long reviewId;

    @NotBlank(message = "Контент отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва не может быть пустым")
    @JsonProperty("isPositive")
    private boolean isPositive;

    @NotNull(message = "ID пользователя не может быть пустым")
    private long userId;

    @NotNull(message = "ID фильма не может быть пустым")
    private long filmId;

    @Builder.Default
    private int useful = 0;
}

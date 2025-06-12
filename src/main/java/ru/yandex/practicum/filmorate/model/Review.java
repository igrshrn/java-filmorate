package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
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
    private Boolean isPositive;

    @NotNull(message = "ID пользователя не может быть пустым")
    private Long userId;

    @NotNull(message = "ID фильма не может быть пустым")
    private Long filmId;

    @Builder.Default
    private int useful = 0;

    @JsonCreator
    public Review(
            @JsonProperty("reviewId") long reviewId,
            @JsonProperty("content") String content,
            @JsonProperty("isPositive") Boolean isPositive,
            @JsonProperty("userId") Long userId,
            @JsonProperty("filmId") Long filmId,
            @JsonProperty("useful") int useful) {
        this.reviewId = reviewId;
        this.content = content;
        this.isPositive = isPositive;
        this.userId = userId;
        this.filmId = filmId;
        this.useful = useful;
    }
}

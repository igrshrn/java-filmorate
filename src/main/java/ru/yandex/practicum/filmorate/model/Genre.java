package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Genre {
    private long id;

    @NotBlank(message = "Жанр не может быть пустым или содержать пробелы")
    private String name;
}


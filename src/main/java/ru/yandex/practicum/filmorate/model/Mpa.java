package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Mpa {
    private long id;

    @NotBlank(message = "Наименование рейтинга не может быть пустым или содержать пробелы")
    private String name;
}

package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Director {
    private long id;

    @NotBlank(message = "Имя режиссера не может быть пустым или содержать пробелы")
    private String name;
}
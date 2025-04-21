package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.utils.validator.user.ValidUser;

import java.time.LocalDate;

@Data
@Builder
@ValidUser
public class User {
    private long id;

    @NotBlank(message = "Email не может быть пустым или содержать пробелы")
    @Email(message = "Email должен быть в правильном формате")
    private String email;

    @NotBlank(message = "Логин не может быть пустым и не может содержать пробелы")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения не может быть пустой")
    private LocalDate birthday;
}

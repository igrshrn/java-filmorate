package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.utils.validator.user.ValidUser;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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

    @Builder.Default
    private Map<Long, Boolean> friends = new HashMap<>();

    @JsonCreator
    public User(
            @JsonProperty("id") long id,
            @JsonProperty("email") String email,
            @JsonProperty("login") String login,
            @JsonProperty("name") String name,
            @JsonProperty("birthday") LocalDate birthday,
            @JsonProperty("friends") Map<Long, Boolean> friends) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.friends = friends != null ? friends : new HashMap<>();
    }

}

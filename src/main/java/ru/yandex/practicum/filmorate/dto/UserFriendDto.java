package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class UserFriendDto {
    private long id;
    private String email;
    private String login;
    private String name;
}

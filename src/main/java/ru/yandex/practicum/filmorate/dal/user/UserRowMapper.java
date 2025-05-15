package ru.yandex.practicum.filmorate.dal.user;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

@Component
public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("user_id"))
                .email(rs.getString("user_email"))
                .login(rs.getString("user_login"))
                .name(rs.getString("user_name"))
                .birthday(rs.getDate("user_birthday").toLocalDate())
                .friends(new HashMap<>())
                .build();
    }
}

package ru.yandex.practicum.filmorate.dal.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserResultSetExtractor implements ResultSetExtractor<Map<Long, User>> {
    private final UserRowMapper mapper;

    @Autowired
    public UserResultSetExtractor(UserRowMapper mapper) {
        this.mapper = mapper;
    }
    @Override
    public Map<Long, User> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, User> users = new HashMap<>();

        while (rs.next()) {
            long userId = rs.getLong("user_id");

            User user = users.computeIfAbsent(userId, k -> {
                try {
                    return mapper.mapRow(rs, 1);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            Long friendId = rs.getObject("friend_id", Long.class);
            Boolean status = rs.getObject("status", Boolean.class);

            if (friendId != null) {
                user.getFriends().put(friendId, status != null ? status : false);
            }
        }

        return users;
    }
}

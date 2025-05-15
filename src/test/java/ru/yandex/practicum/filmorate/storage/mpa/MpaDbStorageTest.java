package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dal.mpa.MpaResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.mpa.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class, MpaResultSetExtractor.class, MpaRowMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MpaDbStorageTest {
    @Autowired
    private MpaDbStorage mpaDbStorage;

    @Test
    void getAll() {
        List<Mpa> mpaList = mpaDbStorage.getAll();
        assertThat(mpaList.size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void getById() {
        Optional<Mpa> mpa = mpaDbStorage.getById(1);
        assertThat(mpa)
                .isPresent()
                .hasValueSatisfying(m -> {
                    assertThat(m.getId()).isEqualTo(1);
                    assertThat(m.getName()).isEqualTo("G");
                });
    }
}
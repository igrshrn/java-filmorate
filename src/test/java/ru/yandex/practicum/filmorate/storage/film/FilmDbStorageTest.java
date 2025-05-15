package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dal.film.FilmResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.user.UserResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.user.UserRowMapper;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.utils.RandomUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, UserDbStorage.class, FilmResultSetExtractor.class, FilmRowMapper.class, UserResultSetExtractor.class, UserRowMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;
    @Autowired
    private UserDbStorage userDbStorage;
    protected RandomUtils randomUtils = new RandomUtils();

    @Test
    void create() {
        Film film = randomUtils.getFilm();
        Film createdFilm = filmDbStorage.create(film);

        assertThat(createdFilm.getName()).isEqualTo(film.getName());
        assertThat(createdFilm.getDescription()).isEqualTo(film.getDescription());

    }

    @Test
    void update() {
        Film film = filmDbStorage.create(randomUtils.getFilm());
        film.setName("Updated name");
        film.setDescription("Updated description");

        Film updated = filmDbStorage.update(film);

        assertThat(updated.getName()).isEqualTo("Updated name");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void getFilmById() {
        Film film = filmDbStorage.create(randomUtils.getFilm());

        Optional<Film> optionalFilm = filmDbStorage.getFilmById(film.getId());

        assertThat(optionalFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("id", film.getId())
                );
    }

    @Test
    void getAll() {
        filmDbStorage.create(randomUtils.getFilm());
        filmDbStorage.create(randomUtils.getFilm());

        Collection<Film> films = filmDbStorage.getAll();
        assertThat(films.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void delete() {
        Film film = filmDbStorage.create(randomUtils.getFilm());
        filmDbStorage.delete(film.getId());

        Optional<Film> optionalFilm = filmDbStorage.getFilmById(film.getId());
        assertThat(optionalFilm).isNotPresent();
    }

    @Test
    void getPopularFilms() {
        Film film1 = filmDbStorage.create(randomUtils.getFilm());
        Film film2 = filmDbStorage.create(randomUtils.getFilm());
        Film film3 = filmDbStorage.create(randomUtils.getFilm());
        Film film4 = filmDbStorage.create(randomUtils.getFilm());

        User user1 = userDbStorage.create(randomUtils.getUser());
        User user2 = userDbStorage.create(randomUtils.getUser());
        User user3 = userDbStorage.create(randomUtils.getUser());

        /**
         * 3 лайка 2 фильму
         * 2 лайка 3 фильму
         * 1 лайк 1 фильму
         */
        filmDbStorage.addLike(film3.getId(), user1.getId());
        filmDbStorage.addLike(film3.getId(), user2.getId());

        filmDbStorage.addLike(film2.getId(), user1.getId());
        filmDbStorage.addLike(film2.getId(), user2.getId());
        filmDbStorage.addLike(film2.getId(), user3.getId());

        filmDbStorage.addLike(film1.getId(), user1.getId());

        Collection<FilmDto> popular = filmDbStorage.getPopularFilms(3);
        List<FilmDto> popularList = popular.stream().toList();

        /**
         * Создано 4 фильма, проверка на ограничения по кол-ву возвращаемых фильмов
         */
        assertThat(popularList.size()).isEqualTo(3);

        /**
         * 2 фильм - на первой позиции, кол-во лайков 3
         */
        assertThat(popularList.get(0).getId()).isEqualTo(2);
        assertThat(popularList.get(0).getLikesCount()).isEqualTo(3);

        /**
         * 3 фильм - на второй позиции, кол-во лайков 2
         */
        assertThat(popularList.get(1).getId()).isEqualTo(3);
        assertThat(popularList.get(1).getLikesCount()).isEqualTo(2);

        /**
         * 1 фильм - на третьей позиции, кол-во лайков 1
         */
        assertThat(popularList.get(2).getId()).isEqualTo(1);
        assertThat(popularList.get(2).getLikesCount()).isEqualTo(1);
    }

    @Test
    void addLike() {
        Film film = filmDbStorage.create(randomUtils.getFilm());
        User user = userDbStorage.create(randomUtils.getUser());

        // Получаем фильм и проверяем, что изначально у него нет лайков
        Optional<Film> filmBeforeAdd = filmDbStorage.getFilmById(film.getId());
        assertThat(filmBeforeAdd)
                .isPresent()
                .hasValueSatisfying(filmBefore ->
                        assertThat(filmBefore.getLikes()).isEmpty()
                );

        // Добавляем лайка фильму
        filmDbStorage.addLike(film.getId(), user.getId());

        Optional<Film> filmAfterAdd = filmDbStorage.getFilmById(film.getId());
        assertThat(filmAfterAdd)
                .isPresent()
                .hasValueSatisfying(filmAfter ->
                        assertThat(filmAfter.getLikes()).isNotEmpty()
                );
    }

    @Test
    void removeLike() {
        Film film = filmDbStorage.create(randomUtils.getFilm());
        User user = userDbStorage.create(randomUtils.getUser());

        filmDbStorage.addLike(film.getId(), user.getId());

        Collection<FilmDto> popularBeforeRemove = filmDbStorage.getPopularFilms(1);
        assertThat(popularBeforeRemove.iterator().next().getLikesCount()).isEqualTo(1);

        filmDbStorage.removeLike(film.getId(), user.getId());

        Collection<FilmDto> popularAfterRemove = filmDbStorage.getPopularFilms(1);
        assertThat(popularAfterRemove.iterator().next().getLikesCount()).isEqualTo(0);
    }
}
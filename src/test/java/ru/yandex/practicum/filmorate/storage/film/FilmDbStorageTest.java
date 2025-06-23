package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dal.director.DirectorResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.film.FilmDtoResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.film.FilmDtoRowMapper;
import ru.yandex.practicum.filmorate.dal.film.FilmResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.user.UserResultSetExtractor;
import ru.yandex.practicum.filmorate.dal.user.UserRowMapper;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.utils.RandomUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, UserDbStorage.class, FilmResultSetExtractor.class, FilmRowMapper.class, FilmDtoResultSetExtractor.class, FilmDtoRowMapper.class, UserResultSetExtractor.class, DirectorResultSetExtractor.class, DirectorDbStorage.class, UserRowMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;
    @Autowired
    private UserDbStorage userDbStorage;
    protected RandomUtils randomUtils = new RandomUtils();
    @Autowired
    private DirectorDbStorage directorDbStorage;

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

        Collection<FilmDto> popular = filmDbStorage.getPopularFilms(3,null,null);
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

        Collection<FilmDto> popularBeforeRemove = filmDbStorage.getPopularFilms(1,null,null);
        assertThat(popularBeforeRemove.iterator().next().getLikesCount()).isEqualTo(1);

        filmDbStorage.removeLike(film.getId(), user.getId());

        Collection<FilmDto> popularAfterRemove = filmDbStorage.getPopularFilms(1,null,null);
        assertThat(popularAfterRemove.iterator().next().getLikesCount()).isEqualTo(0);
    }

    @Test
    void searchFilmsByTitle() {
        Film film1 = randomUtils.getFilm();
        film1.setName("Крадущийся в ночи");
        film1 = filmDbStorage.create(film1);

        Film film2 = randomUtils.getFilm();
        film2.setName("Интерстеллар");
        film2 = filmDbStorage.create(film2);

        Film film3 = randomUtils.getFilm();
        film3.setName("Крёстный отец");
        film3 = filmDbStorage.create(film3);

        String searchQuery = extractSubstring(film1.getName(), 0, 4);
        Collection<Film> searchResults = filmDbStorage.searchFilms(searchQuery, List.of("title"));

        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.iterator().next().getId()).isEqualTo(film1.getId());
    }

    @Test
    void searchFilmsByDirector() {
        Director director1 = directorDbStorage.create(Director.builder().name("Кристофер Нолан").build());
        Director director2 = directorDbStorage.create(Director.builder().name("Квентин Тарантино").build());
        Director director3 = directorDbStorage.create(Director.builder().name("Энг Ли").build());

        Film film1 = randomUtils.getFilm();
        film1.setName("Начало");
        film1.setDirectors(Set.of(director1));
        film1 = filmDbStorage.create(film1);

        Film film2 = randomUtils.getFilm();
        film2.setName("Криминальное чтиво");
        film2.setDirectors(Set.of(director2));
        film2 = filmDbStorage.create(film2);

        Film film3 = randomUtils.getFilm();
        film3.setName("Крадущийся тигр, затаившийся дракон");
        film3.setDirectors(Set.of(director3));
        film3 = filmDbStorage.create(film3);

        String searchQuery = extractSubstring(director1.getName(), 10, 5);
        Collection<Film> searchResults = filmDbStorage.searchFilms(searchQuery, List.of("director"));

        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.iterator().next().getId()).isEqualTo(film1.getId());
        assertThat(searchResults.iterator().next().getDirectors()).contains(director1);
    }

    private String extractSubstring(String input, int startIndex, int length) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        int endIndex = Math.min(startIndex + length, input.length());
        return input.substring(startIndex, endIndex).toLowerCase();
    }
}
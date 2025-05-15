package ru.yandex.practicum.filmorate.utils;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomUtils {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();

    private static final List<Mpa> MPA = List.of(
            Mpa.builder().id(1L).name("G").build(),
            Mpa.builder().id(2L).name("PG").build(),
            Mpa.builder().id(3L).name("PG-13").build(),
            Mpa.builder().id(4L).name("R").build(),
            Mpa.builder().id(5L).name("NC-17").build()
    );

    private static final List<Genre> GENRES = List.of(
            Genre.builder().id(1L).name("Комедия").build(),
            Genre.builder().id(2L).name("Драма").build(),
            Genre.builder().id(3L).name("Мультфильм").build(),
            Genre.builder().id(4L).name("Триллер").build(),
            Genre.builder().id(5L).name("Документальный").build(),
            Genre.builder().id(6L).name("Боевик").build()
    );

    public User getUser() {
        return User.builder()
                .name(getRandomFullName())
                .login(getWord(10))
                .email(getRandomEmail())
                .birthday(getRandomDate())
                .build();
    }

    public Film getFilm() {
        return Film.builder()
                .name(getWord(15))
                .description(getWord(50))
                .releaseDate(getRandomDate())
                .duration(getRandomDuration())
                .genres(getRandomGenres())
                .mpa(getRandomMpa())
                .build();
    }

    public Mpa getRandomMpa() {
        return MPA.get(RANDOM.nextInt(MPA.size()));
    }

    public Set<Genre> getRandomGenres() {
        int numberOfGenres = RANDOM.nextInt(GENRES.size()) + 1;
        Set<Genre> randomGenres = new HashSet<>();

        while (randomGenres.size() < numberOfGenres) {
            Genre randomGenre = GENRES.get(RANDOM.nextInt(GENRES.size()));
            randomGenres.add(randomGenre);
        }

        return randomGenres;
    }

    public int getRandomNumber(int number) {
        return RANDOM.nextInt(number) + 2;
    }

    private LocalDate getRandomDate() {
        long start = LocalDate.of(1970, 1, 1).toEpochDay();
        long end = LocalDate.of(2024, 1, 1).toEpochDay();
        long randomDay = start + RANDOM.nextLong() % (end - start + 1);
        return LocalDate.ofEpochDay(randomDay);
    }

    private String getRandomFullName() {
        return getWord(3 + RANDOM.nextInt(5))
                + " "
                + getWord(3 + RANDOM.nextInt(5));
    }

    private String getRandomEmail() {
        return getWord(3 + RANDOM.nextInt(5))
                + "@"
                + getWord(3 + RANDOM.nextInt(5))
                + "."
                + getWord(3 + RANDOM.nextInt(5));
    }

    private int getRandomDuration() {
        return 60 + RANDOM.nextInt(120);
    }

    public String getWord(int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            result.append(CHARACTERS.charAt(index));
        }
        return result.toString();
    }
}

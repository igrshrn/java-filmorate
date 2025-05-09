package ru.yandex.practicum.filmorate.utils;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Random;

public class RandomUtils {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();

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
                .build();
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

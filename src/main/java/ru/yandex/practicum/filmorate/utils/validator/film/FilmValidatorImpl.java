package ru.yandex.practicum.filmorate.utils.validator.film;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Slf4j
public class FilmValidatorImpl implements ConstraintValidator<ValidFilm, Film> {
    @Override
    public void initialize(ValidFilm constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Film film, ConstraintValidatorContext context) {
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Описание фильма не может быть длиннее 200 символов");
            context.buildConstraintViolationWithTemplate("Описание фильма не может быть длиннее 200 символов")
                    .addPropertyNode("description")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата релиза фильма не может быть раньше 28 декабря 1895 года");
            context.buildConstraintViolationWithTemplate("Дата релиза фильма не может быть раньше 28 декабря 1895 года")
                    .addPropertyNode("releaseDate")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }
}

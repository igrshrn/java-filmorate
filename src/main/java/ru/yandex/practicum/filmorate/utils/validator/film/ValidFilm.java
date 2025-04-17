package ru.yandex.practicum.filmorate.utils.validator.film;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FilmValidatorImpl.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFilm {
    String message() default "Некорректный фильм";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
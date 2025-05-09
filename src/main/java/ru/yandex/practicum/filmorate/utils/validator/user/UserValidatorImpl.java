package ru.yandex.practicum.filmorate.utils.validator.user;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Slf4j
public class UserValidatorImpl implements ConstraintValidator<ValidUser, User> {
    private static final Pattern LATIN_SYMBOL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    public void initialize(ValidUser constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения пользователя не может быть в будущем");
            constraintValidatorContext.buildConstraintViolationWithTemplate("Дата рождения пользователя не может быть в будущем")
                    .addPropertyNode("birthday")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        if (user.getEmail() != null && !LATIN_SYMBOL_PATTERN.matcher(user.getEmail()).matches()) {
            log.error("Email должен содержать только латинские буквы");
            constraintValidatorContext.buildConstraintViolationWithTemplate("Email должен содержать только латинские буквы")
                    .addPropertyNode("email")
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }
}

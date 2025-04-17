package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractValidationTest<T> {
    protected Validator validator;

    @BeforeEach
    void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    protected Map<String, List<String>> violationsToMap(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getPropertyPath().toString(),
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())
                ));
    }
}

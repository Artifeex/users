package ru.sandr.users.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.sandr.users.core.validation.validator.OptionalNotBlankValidator;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OptionalNotBlankValidator.class)
public @interface OptionalNotBlank {
    String message() default "Поле не может быть пустым или состоять только из пробелов";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
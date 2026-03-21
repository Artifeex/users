package ru.sandr.users.core.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.sandr.users.core.validation.OptionalNotBlank;

public class OptionalNotBlankValidator implements ConstraintValidator<OptionalNotBlank, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 1. Если null — значит клиент просто не прислал поле (PATCH). Это ОК!
        if (value == null) {
            return true;
        }

        // 2. Если поле прислали, используем встроенный в Java метод isBlank().
        // Он вернет true, если строка пустая ("") или состоит только из пробелов/табов ("   ").
        // Нам нужно, чтобы она БЫЛА НЕ пустой, поэтому ставим отрицание (!).
        return !value.isBlank();
    }
}

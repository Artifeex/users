package ru.sandr.users.core.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Getter
public class CustomException extends RuntimeException {
    private final String code;
    private final Map<String, String> placeHolders;

    public CustomException(String code, String message) {
        this(code, message, Map.of());
    }

    public CustomException(String code, String message, Map<String, ?> placeHolders) {
        super(message);
        this.code = code;
        this.placeHolders = mapToStringValues(placeHolders);
    }

    private static Map<String, String> mapToStringValues(Map<String, ?> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }

        Map<String, String> mappedValues = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            if (key != null && value != null) {
                mappedValues.put(key, Objects.toString(value));
            }
        });

        if (mappedValues.isEmpty()) {
            return Map.of();
        }

        return Collections.unmodifiableMap(mappedValues);
    }
}


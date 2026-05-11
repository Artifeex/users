package ru.sandr.users.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Getter
public class ApiErrorResponse {

    private final String errorCode;
    private final String debugErrorMessage;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, String> placeHolders;

    public ApiErrorResponse(String debugErrorMessage, String errorCode) {
        this(debugErrorMessage, errorCode, Map.of());
    }

    public ApiErrorResponse(String debugErrorMessage, String errorCode, Map<String, ?> placeHolders) {
        this.errorCode = errorCode;
        this.debugErrorMessage = debugErrorMessage;
        this.placeHolders = new LinkedHashMap<>();
        addPlaceHolders(placeHolders);
    }

    public void addPlaceHolder(String key, Object value) {
        if (key == null || value == null) {
            return;
        }
        placeHolders.put(key, Objects.toString(value));
    }

    public void addPlaceHolders(Map<String, ?> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        values.forEach(this::addPlaceHolder);
    }
}

package ru.sandr.users.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Schema(description = "Standard error response returned by GlobalExceptionHandler")
public class ApiErrorResponse {

    @Schema(description = "Machine-readable error code", example = "OBJECT_NOT_FOUND")
    private final String errorCode;
    @Schema(description = "Debug-oriented error message", example = "User with id 4b4f60a5-7b67-44f3-9e5a-9df45f77f653 was not found")
    private final String debugErrorMessage;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "Named values used for message interpolation", example = "{\"id\":\"4b4f60a5-7b67-44f3-9e5a-9df45f77f653\"}")
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

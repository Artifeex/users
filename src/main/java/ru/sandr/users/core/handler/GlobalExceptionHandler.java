package ru.sandr.users.core.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.ObjectError;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.sandr.users.core.dto.ApiErrorResponse;
import ru.sandr.users.core.exception.*;
import ru.sandr.users.imports.exception.ImportValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleUnauthorized(UnauthorizedException ex) {
        return fromCustomException(ex);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleBadCredentials(BadCredentialsException ex) {
        // Return abstract message for security reasons.
        var response = new ApiErrorResponse("Invalid username or password", "BAD_CREDENTIALS");
        response.addPlaceHolder("reason", "invalid_credentials");
        return response;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleAccessDenied(AccessDeniedException ex) {
        return fromCustomException(ex);
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNotFound(ObjectNotFoundException ex) {
        return fromCustomException(ex);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleConflict(ConflictException ex) {
        return fromCustomException(ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        var response = new ApiErrorResponse("Request validation failed", "VALIDATION_FAILED");
        var allErrors = ex.getBindingResult().getAllErrors();
        response.addPlaceHolder("violationsCount", allErrors.size());

        for (int i = 0; i < allErrors.size(); i++) {
            addValidationPlaceholders(response, allErrors.get(i), i);
        }

        return response;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMessageNotReadable(HttpMessageNotReadableException ex) {
        ApiErrorResponse response = new ApiErrorResponse(
                "Request body cannot be parsed. Check payload format.",
                "MALFORMED_JSON"
        );

        // Достаем оригинальную ошибку Jackson (причину)
        Throwable cause = ex.getCause();

        // Если причина - несовпадение типов (передали строку вместо числа)
        if (cause instanceof InvalidFormatException invalidFormatException) {
            String fieldName = buildJsonFieldPath(invalidFormatException);
            String expectedType = invalidFormatException.getTargetType() != null
                    ? invalidFormatException.getTargetType().getSimpleName()
                    : "unknown";

            response.addPlaceHolder("field", fieldName);
            response.addPlaceHolder("expectedType", expectedType);
            response.addPlaceHolder("reason", "Invalid data type");
            if (!isSensitiveField(fieldName) && invalidFormatException.getValue() != null) {
                response.addPlaceHolder("actualValue", invalidFormatException.getValue());
            }
        }
        // Если клиент прислал вообще не JSON (например, забыл закрыть скобку '}')
        else {
            response.addPlaceHolder("field", "body");
            response.addPlaceHolder("reason", "Malformed JSON syntax");
        }
        return response;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ApiErrorResponse response = new ApiErrorResponse(
                "Invalid request parameter type",
                "INVALID_ARGUMENT_TYPE"
        );

        String fieldName = ex.getName() != null ? ex.getName() : "argument";
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        response.addPlaceHolder("field", fieldName);
        response.addPlaceHolder("expectedType", expectedType);
        response.addPlaceHolder("reason", "Invalid data type");
        if (!isSensitiveField(fieldName) && ex.getValue() != null) {
            response.addPlaceHolder("actualValue", ex.getValue());
        }
        return response;
    }

    @ExceptionHandler(MissedRequiredArgument.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMissedRequiredArgument(MissedRequiredArgument ex) {
        return fromCustomException(ex);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBadRequest(BadRequestException ex) {
        return fromCustomException(ex);
    }

    @ExceptionHandler(ImportValidationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiErrorResponse handleImportValidation(ImportValidationException ex) {
        ApiErrorResponse response = fromCustomException(ex);
        response.addPlaceHolder("errorsCount", ex.getErrors().size());

        for (int i = 0; i < ex.getErrors().size(); i++) {
            var rowError = ex.getErrors().get(i);
            response.addPlaceHolder("row_" + i, rowError.row());
            response.addPlaceHolder("column_" + i, rowError.column());
            response.addPlaceHolder("reason_" + i, rowError.message());
        }

        return response;
    }

    private ApiErrorResponse fromCustomException(CustomException ex) {
        return new ApiErrorResponse(ex.getMessage(), ex.getCode(), ex.getPlaceHolders());
    }

    private void addValidationPlaceholders(ApiErrorResponse response, ObjectError error, int index) {
        String reason = error.getDefaultMessage() != null ? error.getDefaultMessage() : "Validation rule violated";
        String constraint = extractConstraintCode(error);

        response.addPlaceHolder("reason_" + index, reason);
        response.addPlaceHolder("constraint_" + index, constraint);

        if (error instanceof FieldError fieldError) {
            String field = fieldError.getField();
            response.addPlaceHolder("field_" + index, field);
            if (!isSensitiveField(field) && fieldError.getRejectedValue() != null) {
                response.addPlaceHolder("rejectedValue_" + index, fieldError.getRejectedValue());
            }
            if (index == 0) {
                response.addPlaceHolder("field", field);
            }
        } else {
            response.addPlaceHolder("object_" + index, error.getObjectName());
        }

        if (index == 0) {
            response.addPlaceHolder("reason", reason);
            response.addPlaceHolder("constraint", constraint);
        }
    }

    private String extractConstraintCode(ObjectError error) {
        String[] codes = error.getCodes();
        if (codes == null || codes.length == 0) {
            return "UNKNOWN";
        }

        String firstCode = codes[0];
        int separatorIndex = firstCode.indexOf('.');
        String constraintName = separatorIndex > 0 ? firstCode.substring(0, separatorIndex) : firstCode;
        return constraintName.toUpperCase();
    }

    private String buildJsonFieldPath(InvalidFormatException invalidFormatException) {
        StringBuilder pathBuilder = new StringBuilder();
        invalidFormatException.getPath().forEach(reference -> {
            if (reference.getFieldName() != null) {
                if (!pathBuilder.isEmpty()) {
                    pathBuilder.append('.');
                }
                pathBuilder.append(reference.getFieldName());
            } else if (reference.getIndex() >= 0) {
                pathBuilder.append('[').append(reference.getIndex()).append(']');
            }
        });
        return pathBuilder.isEmpty() ? "body" : pathBuilder.toString();
    }

    private boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        String lowerCaseFieldName = fieldName.toLowerCase();
        return lowerCaseFieldName.contains("password") || lowerCaseFieldName.contains("token");
    }

}

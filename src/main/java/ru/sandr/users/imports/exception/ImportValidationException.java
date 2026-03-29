package ru.sandr.users.imports.exception;

import lombok.Getter;
import ru.sandr.users.core.exception.CustomException;
import ru.sandr.users.imports.dto.ImportRowError;

import java.util.List;

@Getter
public class ImportValidationException extends CustomException {

    private final List<ImportRowError> errors;

    public ImportValidationException(List<ImportRowError> errors) {
        super("IMPORT_VALIDATION_FAILED",
                "Import failed: " + errors.size() + " validation error(s) found");
        this.errors = errors;
    }
}

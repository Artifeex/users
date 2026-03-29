package ru.sandr.users.imports.dto;

import java.util.List;

public record ImportValidationErrorResponse(String message, List<ImportRowError> errors) {}

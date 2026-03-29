package ru.sandr.users.imports.dto;

public record ImportRowError(int row, String column, String message) {}

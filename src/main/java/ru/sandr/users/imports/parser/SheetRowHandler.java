package ru.sandr.users.imports.parser;

@FunctionalInterface
public interface SheetRowHandler {
    void handleRow(int rowIndex, String[] cells);
}

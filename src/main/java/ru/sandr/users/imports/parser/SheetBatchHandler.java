package ru.sandr.users.imports.parser;

import java.util.List;

@FunctionalInterface
public interface SheetBatchHandler {
    void handleBatch(List<ParsedRow> rows);
}

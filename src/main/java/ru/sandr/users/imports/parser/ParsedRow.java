package ru.sandr.users.imports.parser;

/**
 * One data row from a streamed sheet (header row excluded).
 *
 * @param rowIndex physical row index in the sheet (0-based; header is row 0)
 * @param cells    column values left-to-right
 */
public record ParsedRow(int rowIndex, String[] cells) {}

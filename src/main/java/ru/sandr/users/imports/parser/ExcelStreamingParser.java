package ru.sandr.users.imports.parser;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import ru.sandr.users.core.exception.BadRequestException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelStreamingParser {

    /**
     * SAX-streams a single sheet of an .xlsx file, invoking {@code rowHandler} for every
     * data row (row index 0 is treated as the header and is not passed to {@code rowHandler}).
     * Re-callable: pass a fresh {@code InputStream} for each invocation (e.g. Pass 1 / Pass 2).
     */
    public void parse(InputStream inputStream, SheetRowHandler rowHandler) {
        try (OPCPackage pkg = OPCPackage.open(inputStream)) {
            XSSFReader reader = new XSSFReader(pkg);
            var sst = reader.getSharedStringsTable();
            var styles = reader.getStylesTable();

            XMLReader xmlReader = XMLHelper.newXMLReader();
            xmlReader.setContentHandler(new XSSFSheetXMLHandler(
                    styles, null, sst,
                    new RowCollector(rowHandler),
                    new DataFormatter(), false
            ));
            xmlReader.parse(new InputSource(reader.getSheetsData().next()));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("INVALID_FILE_FORMAT",
                    "Cannot process Excel file: " + e.getMessage());
        }
    }

    private static class RowCollector implements XSSFSheetXMLHandler.SheetContentsHandler {

        private final SheetRowHandler rowHandler;
        private final List<String> currentRow = new ArrayList<>();

        RowCollector(SheetRowHandler rowHandler) {
            this.rowHandler = rowHandler;
        }

        @Override
        public void startRow(int rowNum) {
            currentRow.clear();
        }

        @Override
        public void endRow(int rowNum) {
            if (rowNum > 0 && !isBlankRow(currentRow)) { // row 0 is the header
                rowHandler.handleRow(rowNum, currentRow.toArray(new String[0]));
            }
        }

        @Override
        public void cell(String cellRef, String formattedValue, XSSFComment comment) {
            fillCell(currentRow, cellRef, formattedValue);
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {}
    }

    private static void fillCell(List<String> currentRow, String cellRef, String formattedValue) {
        String colLetters = cellRef.replaceAll("\\d", "");
        int col = CellReference.convertColStringToIndex(colLetters);
        while (currentRow.size() <= col) {
            currentRow.add("");
        }
        currentRow.set(col, formattedValue != null ? formattedValue.trim() : "");
    }

    /**
     * Проверяет, является ли собранная строка абсолютно пустой.
     * Возвращает true, если список пуст или содержит только null/пустые строки.
     */
    private static boolean isBlankRow(List<String> row) {
        if (row.isEmpty()) {
            return true;
        }
        for (String cellValue : row) {
            if (cellValue != null && !cellValue.isBlank()) {
                return false; // Нашли хотя бы одно непустое значение
            }
        }
        return true;
    }
}

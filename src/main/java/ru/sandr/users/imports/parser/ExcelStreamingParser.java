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

    /**
     * Like {@link #parse(InputStream, SheetRowHandler)} but invokes {@code batchHandler} with
     * up to {@code batchSize} rows at a time, then any remainder after the last full batch.
     */
    public void parseBatch(InputStream inputStream, int batchSize, SheetBatchHandler batchHandler) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be >= 1");
        }
        List<ParsedRow> buffer = new ArrayList<>(batchSize);
        try (OPCPackage pkg = OPCPackage.open(inputStream)) {
            XSSFReader reader = new XSSFReader(pkg);
            var sst = reader.getSharedStringsTable();
            var styles = reader.getStylesTable();

            XMLReader xmlReader = XMLHelper.newXMLReader();
            xmlReader.setContentHandler(new XSSFSheetXMLHandler(
                    styles, null, sst,
                    new BatchRowCollector(batchSize, buffer, batchHandler),
                    new DataFormatter(), false
            ));
            xmlReader.parse(new InputSource(reader.getSheetsData().next()));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("INVALID_FILE_FORMAT",
                    "Cannot process Excel file: " + e.getMessage());
        }
        if (!buffer.isEmpty()) {
            batchHandler.handleBatch(List.copyOf(buffer));
            buffer.clear();
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
            if (rowNum > 0) { // row 0 is the header
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

    private static class BatchRowCollector implements XSSFSheetXMLHandler.SheetContentsHandler {

        private final int batchSize;
        private final List<ParsedRow> buffer;
        private final SheetBatchHandler batchHandler;
        private final List<String> currentRow = new ArrayList<>();

        BatchRowCollector(int batchSize, List<ParsedRow> buffer, SheetBatchHandler batchHandler) {
            this.batchSize = batchSize;
            this.buffer = buffer;
            this.batchHandler = batchHandler;
        }

        @Override
        public void startRow(int rowNum) {
            currentRow.clear();
        }

        @Override
        public void endRow(int rowNum) {
            if (rowNum > 0) {
                buffer.add(new ParsedRow(rowNum, currentRow.toArray(new String[0])));
                if (buffer.size() == batchSize) {
                    batchHandler.handleBatch(List.copyOf(buffer));
                    buffer.clear();
                }
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
}

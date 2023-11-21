package ru.obukhov.trader.common.model.poi;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFGraphicFrame;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.PoiTestData;

import java.io.IOException;
import java.util.List;

class ExtendedSheetUnitTest {

    // region constructor tests

    @Test
    void constructor_throwsIllegalArgumentException_whenWorkbookIsNull() throws IOException {
        try (final ExtendedWorkbook extendedWorkbook = PoiTestData.createExtendedWorkbook()) {
            final Sheet sheet = extendedWorkbook.createSheet();

            final Executable executable = () -> new ExtendedSheet(null, sheet);
            AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "workbook can't be null");
        }
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenDelegateIsNull() throws IOException {
        try (final ExtendedWorkbook extendedWorkbook = PoiTestData.createExtendedWorkbook()) {

            final Executable executable = () -> new ExtendedSheet(extendedWorkbook, null);
            AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "delegate can't be null");
        }
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenDelegateIsExtendedSheet() throws IOException {
        try (final ExtendedWorkbook extendedWorkbook = PoiTestData.createExtendedWorkbook()) {
            final Sheet sheet = extendedWorkbook.createSheet();

            final Executable executable = () -> new ExtendedSheet(extendedWorkbook, sheet);
            AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "delegate can't be ExtendedSheet");
        }
    }

    @Test
    void constructor_CopiesRows() {
        final Workbook workbook = new XSSFWorkbook();
        final Sheet sheet = workbook.createSheet();
        final String value0 = "row0cell";
        sheet.createRow(0).createCell(0).setCellValue(value0);
        final String value1 = "row1cell";
        sheet.createRow(1).createCell(0).setCellValue(value1);

        final ExtendedWorkbook extendedWorkbook = new ExtendedWorkbook(workbook);

        final ExtendedSheet extendedSheet = new ExtendedSheet(extendedWorkbook, sheet);

        Assertions.assertEquals(2, extendedSheet.getRowsCount());
        Assertions.assertEquals(value0, extendedSheet.getRow(0).getCell(0).getStringCellValue());
        Assertions.assertEquals(value1, extendedSheet.getRow(1).getCell(0).getStringCellValue());
    }

    // endregion

    // region getRowsCount tests

    @Test
    void getRowsCount_returnProperRowsCount_whenThereIsNoGapBetweenRows() throws IOException {
        final ExtendedSheet extendedSheet = PoiTestData.createExtendedSheet();
        PoiTestData.addRows(extendedSheet, 0, 1, 2, 3);

        Assertions.assertEquals(4, extendedSheet.getRowsCount());
    }

    @Test
    void getRowsCount_returnProperRowsCount_whenThereIsGapBetweenRows() throws IOException {
        final ExtendedSheet extendedSheet = PoiTestData.createExtendedSheet();
        PoiTestData.addRows(extendedSheet, 0, 1, 2, 10);

        Assertions.assertEquals(4, extendedSheet.getRowsCount());
    }

    // endregion

    @Test
    void autoSizeColumns_callsAutoSizeColumnOfDelegate() throws IOException {
        try (final Workbook workbook = new XSSFWorkbook()) {
            final Sheet sheet = workbook.createSheet();
            PoiTestData.addRow(sheet, 2);
            PoiTestData.addRow(sheet, 3);
            PoiTestData.addRow(sheet, 1);

            final Sheet sheetMock = Mockito.mock(Sheet.class);
            Mockito.when(sheetMock.spliterator()).thenReturn(sheet.spliterator());

            final ExtendedWorkbook extendedWorkbook = PoiTestData.createExtendedWorkbook();
            ExtendedSheet extendedSheet = new ExtendedSheet(extendedWorkbook, sheetMock);

            extendedSheet.autoSizeColumns();

            Mockito.verify(sheetMock, Mockito.times(1)).autoSizeColumn(0);
            Mockito.verify(sheetMock, Mockito.times(1)).autoSizeColumn(1);
            Mockito.verify(sheetMock, Mockito.times(1)).autoSizeColumn(2);
        }

    }

    // region getColumnsCount tests

    @Test
    void getColumnsCount_returnsZero_whenNoRows() throws IOException {
        final ExtendedSheet extendedSheet = PoiTestData.createExtendedSheet();

        final int columnsCount = extendedSheet.getColumnsCount();

        Assertions.assertEquals(0, columnsCount);
    }

    @Test
    void getColumnsCount_returnsMaxColumnsCountBetweenRows() throws IOException {
        final ExtendedSheet extendedSheet = PoiTestData.createExtendedSheet();
        PoiTestData.addRow(extendedSheet, 2);
        PoiTestData.addRow(extendedSheet, 3);
        PoiTestData.addRow(extendedSheet, 1);

        final int columnsCount = extendedSheet.getColumnsCount();

        Assertions.assertEquals(3, columnsCount);
    }

    // endregion

    @Test
    void addRow_withoutValues_addsRowAfterLastRow() throws IOException {
        final ExtendedSheet extendedSheet = PoiTestData.createExtendedSheet();
        extendedSheet.createRow(0);
        extendedSheet.createRow(1);
        extendedSheet.createRow(4);

        final ExtendedRow extendedRow = extendedSheet.addRow();

        Assertions.assertEquals(5, extendedRow.getRowNum());
        Assertions.assertEquals(5, extendedSheet.getLastRowNum());
        Assertions.assertEquals(4, extendedSheet.getRowsCount());
    }

    @Test
    void addRow_withValues_addsRowAfterLastRow() throws IOException {
        final ExtendedSheet extendedSheet = PoiTestData.createExtendedSheet();
        extendedSheet.createRow(0);
        extendedSheet.createRow(1);
        extendedSheet.createRow(4);

        extendedSheet.addRow("value1", "value2");

        Assertions.assertEquals(5, extendedSheet.getLastRowNum());
        Assertions.assertEquals(4, extendedSheet.getRowsCount());
    }

    @Test
    void addRow_withValues_returnsCells() throws IOException {
        final ExtendedSheet extendedSheet = PoiTestData.createExtendedSheet();
        extendedSheet.createRow(0);
        extendedSheet.createRow(1);
        extendedSheet.createRow(4);

        final String value1 = "value1";
        final String value2 = "value2";

        List<ExtendedCell> cells = extendedSheet.addRow(value1, value2);

        Assertions.assertEquals(2, cells.size());
        Assertions.assertEquals(value1, cells.get(0).getStringCellValue());
        Assertions.assertEquals(value2, cells.get(1).getStringCellValue());
    }

    @Test
    void createChart() throws IOException {
        final ExtendedSheet extendedSheet = PoiTestData.createExtendedSheet();
        final int column1 = 1;
        final int row1 = 2;
        final int column2 = 3;
        final int row2 = 4;

        final ExtendedChart chart = extendedSheet.createChart(column1, row1, column2, row2);

        Assertions.assertNotNull(chart);

        final Drawing<?> drawing = extendedSheet.getDrawingPatriarch();
        final List<?> frames = IteratorUtils.toList(drawing.iterator());
        Assertions.assertEquals(1, frames.size());
        final XSSFGraphicFrame frame = (XSSFGraphicFrame) frames.get(0);

        final ClientAnchor anchor = frame.getAnchor();
        Assertions.assertEquals(column1, anchor.getCol1());
        Assertions.assertEquals(row1, anchor.getRow1());
        Assertions.assertEquals(column2, anchor.getCol2());
        Assertions.assertEquals(row2, anchor.getRow2());

        final List<XSSFChart> charts = ((XSSFDrawing) drawing).getCharts();
        Assertions.assertEquals(1, charts.size());
        Assertions.assertEquals(chart.getDelegate(), charts.get(0));
    }

}
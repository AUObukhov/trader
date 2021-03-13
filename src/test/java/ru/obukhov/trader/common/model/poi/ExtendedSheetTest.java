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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class ExtendedSheetTest {

    // region constructor tests

    @Test
    void constructor_throwsIllegalArgumentException_whenWorkbookIsNull() {
        ExtendedWorkbook extendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();
        Sheet sheet = extendedWorkbook.createSheet();

        AssertUtils.assertThrowsWithMessage(() -> new ExtendedSheet(null, sheet),
                IllegalArgumentException.class,
                "workbook can't be null");
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenDelegateIsNull() {
        ExtendedWorkbook extendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();

        AssertUtils.assertThrowsWithMessage(() -> new ExtendedSheet(extendedWorkbook, null),
                IllegalArgumentException.class,
                "delegate can't be null");
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenDelegateIsExtendedSheet() {
        ExtendedWorkbook extendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();
        Sheet sheet = extendedWorkbook.createSheet();

        AssertUtils.assertThrowsWithMessage(() -> new ExtendedSheet(extendedWorkbook, sheet),
                IllegalArgumentException.class,
                "delegate can't be ExtendedSheet");
    }

    @Test
    void constructor_CopiesRows() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        String value0 = "row0cell";
        sheet.createRow(0).createCell(0).setCellValue(value0);
        String value1 = "row1cell";
        sheet.createRow(1).createCell(0).setCellValue(value1);

        ExtendedWorkbook extendedWorkbook = new ExtendedWorkbook(workbook);

        ExtendedSheet extendedSheet = new ExtendedSheet(extendedWorkbook, sheet);

        assertEquals(2, extendedSheet.getRowsCount());
        assertEquals(value0, extendedSheet.getRow(0).getCell(0).getStringCellValue());
        assertEquals(value1, extendedSheet.getRow(1).getCell(0).getStringCellValue());
    }

    // endregion

    // region getRowsCount tests

    @Test
    void getRowsCount_returnProperRowsCount_whenThereIsNoGapBetweenRows() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        ExcelTestDataHelper.addRows(extendedSheet, 0, 1, 2, 3);

        assertEquals(4, extendedSheet.getRowsCount());
    }

    @Test
    void getRowsCount_returnProperRowsCount_whenThereIsGapBetweenRows() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        ExcelTestDataHelper.addRows(extendedSheet, 0, 1, 2, 10);

        assertEquals(4, extendedSheet.getRowsCount());
    }

    // endregion

    @Test
    void autoSizeColumns_callsAutoSizeColumnOfDelegate() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        ExcelTestDataHelper.addRow(sheet, 2);
        ExcelTestDataHelper.addRow(sheet, 3);
        ExcelTestDataHelper.addRow(sheet, 1);

        Sheet sheetMock = Mockito.mock(Sheet.class);
        when(sheetMock.rowIterator()).thenReturn(sheet.rowIterator());

        ExtendedWorkbook extendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();
        ExtendedSheet extendedSheet = new ExtendedSheet(extendedWorkbook, sheetMock);

        extendedSheet.autoSizeColumns();

        Mockito.verify(sheetMock, times(1)).autoSizeColumn(eq(0));
        Mockito.verify(sheetMock, times(1)).autoSizeColumn(eq(1));
        Mockito.verify(sheetMock, times(1)).autoSizeColumn(eq(2));
    }

    // region getColumnsCount tests

    @Test
    void getColumnsCount_returnsZero_whenNoRows() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();

        int columnsCount = extendedSheet.getColumnsCount();

        assertEquals(0, columnsCount);
    }

    @Test
    void getColumnsCount_returnsMaxColumnsCountBetweenRows() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        ExcelTestDataHelper.addRow(extendedSheet, 2);
        ExcelTestDataHelper.addRow(extendedSheet, 3);
        ExcelTestDataHelper.addRow(extendedSheet, 1);

        int columnsCount = extendedSheet.getColumnsCount();

        assertEquals(3, columnsCount);
    }

    // endregion

    @Test
    void addRow_addsRowAfterLastRow() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        extendedSheet.createRow(0);
        extendedSheet.createRow(1);
        extendedSheet.createRow(4);

        ExtendedRow extendedRow = extendedSheet.addRow();

        assertEquals(5, extendedRow.getRowNum());
        assertEquals(5, extendedSheet.getLastRowNum());
        assertEquals(4, extendedSheet.getRowsCount());
    }

    @Test
    void createChart() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        int column1 = 1;
        int row1 = 2;
        int column2 = 3;
        int row2 = 4;

        ExtendedChart chart = extendedSheet.createChart(column1, row1, column2, row2);

        assertNotNull(chart);

        Drawing<?> drawing = extendedSheet.getDrawingPatriarch();
        List<?> frames = IteratorUtils.toList(drawing.iterator());
        assertEquals(1, frames.size());
        XSSFGraphicFrame frame = (XSSFGraphicFrame) frames.get(0);

        ClientAnchor anchor = frame.getAnchor();
        assertEquals(column1, anchor.getCol1());
        assertEquals(row1, anchor.getRow1());
        assertEquals(column2, anchor.getCol2());
        assertEquals(row2, anchor.getRow2());

        List<XSSFChart> charts = ((XSSFDrawing) drawing).getCharts();
        assertEquals(1, charts.size());
        assertEquals(chart.getDelegate(), charts.get(0));
    }

}
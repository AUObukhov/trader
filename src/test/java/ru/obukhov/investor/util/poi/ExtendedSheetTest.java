package ru.obukhov.investor.util.poi;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class ExtendedSheetTest {

    // region constructor tests

    @Test(expected = IllegalArgumentException.class)
    public void constructor_throwsIllegalArgumentException_whenWorkbookIsNull() {
        ExtendedWorkbook extendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();
        Sheet sheet = extendedWorkbook.createSheet();

        new ExtendedSheet(null, sheet);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_throwsIllegalArgumentException_whenDelegateIsNull() {
        ExtendedWorkbook extendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();

        new ExtendedSheet(extendedWorkbook, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_throwsIllegalArgumentException_whenDelegateIsExtendedSheet() {
        ExtendedWorkbook extendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();
        Sheet sheet = extendedWorkbook.createSheet();

        new ExtendedSheet(extendedWorkbook, sheet);
    }

    @Test
    public void constructor_CopiesRows() {
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
    public void getRowsCount_returnProperRowsCount_whenThereIsNoGapBetweenRows() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        ExcelTestDataHelper.addRows(extendedSheet, 0, 1, 2, 3);

        assertEquals(4, extendedSheet.getRowsCount());
    }

    @Test
    public void getRowsCount_returnProperRowsCount_whenThereIsGapBetweenRows() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        ExcelTestDataHelper.addRows(extendedSheet, 0, 1, 2, 10);

        assertEquals(4, extendedSheet.getRowsCount());
    }

    // endregion

    @Test
    public void autoSizeColumns_callsAutoSizeColumnOfDelegate() {
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
    public void getColumnsCount_returnsZero_whenNoRows() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();

        int columnsCount = extendedSheet.getColumnsCount();

        assertEquals(0, columnsCount);
    }

    @Test
    public void getColumnsCount_returnsMaxColumnsCountBetweenRows() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        ExcelTestDataHelper.addRow(extendedSheet, 2);
        ExcelTestDataHelper.addRow(extendedSheet, 3);
        ExcelTestDataHelper.addRow(extendedSheet, 1);

        int columnsCount = extendedSheet.getColumnsCount();

        assertEquals(3, columnsCount);
    }

    // endregion

    @Test
    public void addRow_addsRowAfterLastRow() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        extendedSheet.createRow(0);
        extendedSheet.createRow(1);
        extendedSheet.createRow(4);

        ExtendedRow extendedRow = extendedSheet.addRow();

        assertEquals(5, extendedRow.getRowNum());
        assertEquals(5, extendedSheet.getLastRowNum());
        assertEquals(4, extendedSheet.getRowsCount());
    }

}
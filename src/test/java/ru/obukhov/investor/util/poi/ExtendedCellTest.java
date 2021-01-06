package ru.obukhov.investor.util.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class ExtendedCellTest {

    // region constructor tests

    @Test(expected = IllegalArgumentException.class)
    public void constructor_throwsIllegalArgumentException_whenRowIsNull() {
        Sheet sheet = ExcelTestDataHelper.createXSSFSheet();
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);

        new ExtendedCell(null, cell);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_throwsIllegalArgumentException_whenDelegateIsNull() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        ExtendedRow extendedRow = extendedSheet.addRow();

        new ExtendedCell(extendedRow, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_throwsIllegalArgumentException_whenDelegateIsExtendedCell() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        ExtendedRow extendedRow = extendedSheet.addRow();
        Cell extendedCell = extendedRow.createCell(0);

        new ExtendedCell(extendedRow, extendedCell);
    }

    // endregion

    // region getWorkbook tests

    @Test
    public void getWorkbook_returnsParentWorkbook() {
        ExtendedWorkbook parentExtendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();
        ExtendedSheet extendedSheet = (ExtendedSheet) parentExtendedWorkbook.createSheet();
        ExtendedCell extendedCell = (ExtendedCell) extendedSheet.addRow().createCell(0);

        ExtendedWorkbook returnedExtendedWorkbook = extendedCell.getWorkbook();

        assertSame(parentExtendedWorkbook, returnedExtendedWorkbook);
    }

    // endregion

}
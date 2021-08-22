package ru.obukhov.trader.common.model.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.obukhov.trader.test.utils.AssertUtils;

class ExtendedCellUnitTest {

    // region constructor tests

    @Test
    void constructor_throwsIllegalArgumentException_whenRowIsNull() {
        final Sheet sheet = ExcelTestDataHelper.createXSSFSheet();
        final Row row = sheet.createRow(0);
        final Cell cell = row.createCell(0);

        final Executable executable = () -> new ExtendedCell(null, cell);
        AssertUtils.assertThrowsWithMessage(executable, IllegalArgumentException.class, "row can't be null");
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenDelegateIsNull() {
        final ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        final ExtendedRow extendedRow = extendedSheet.addRow();

        final Executable executable = () -> new ExtendedCell(extendedRow, null);
        AssertUtils.assertThrowsWithMessage(executable, IllegalArgumentException.class, "delegate can't be null");
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenDelegateIsExtendedCell() {
        final ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        final ExtendedRow extendedRow = extendedSheet.addRow();
        final Cell extendedCell = extendedRow.createCell(0);

        final Executable executable = () -> new ExtendedCell(extendedRow, extendedCell);
        AssertUtils.assertThrowsWithMessage(executable, IllegalArgumentException.class, "delegate can't be ExtendedCell");
    }

    // endregion

    // region getWorkbook tests

    @Test
    void getWorkbook_returnsParentWorkbook() {
        final ExtendedWorkbook parentExtendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();
        final ExtendedSheet extendedSheet = (ExtendedSheet) parentExtendedWorkbook.createSheet();
        final ExtendedCell extendedCell = (ExtendedCell) extendedSheet.addRow().createCell(0);

        final ExtendedWorkbook returnedExtendedWorkbook = extendedCell.getWorkbook();

        Assertions.assertSame(parentExtendedWorkbook, returnedExtendedWorkbook);
    }

    // endregion

}
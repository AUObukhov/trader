package ru.obukhov.trader.common.model.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.obukhov.trader.test.utils.PoiTestData;

class ExtendedCellUnitTest {

    // region constructor tests

    @Test
    void constructor_throwsIllegalArgumentException_whenRowIsNull() {
        final Sheet sheet = PoiTestData.createXSSFSheet();
        final Row row = sheet.createRow(0);
        final Cell cell = row.createCell(0);

        final Executable executable = () -> new ExtendedCell(null, cell);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "row can't be null");
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenDelegateIsNull() {
        final ExtendedSheet extendedSheet = PoiTestData.createExtendedSheet();
        final ExtendedRow extendedRow = extendedSheet.addRow();

        final Executable executable = () -> new ExtendedCell(extendedRow, null);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "delegate can't be null");
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenDelegateIsExtendedCell() {
        final ExtendedSheet extendedSheet = PoiTestData.createExtendedSheet();
        final ExtendedRow extendedRow = extendedSheet.addRow();
        final Cell extendedCell = extendedRow.createCell(0);

        final Executable executable = () -> new ExtendedCell(extendedRow, extendedCell);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "delegate can't be ExtendedCell");
    }

    // endregion

    // region getWorkbook tests

    @Test
    void getWorkbook_returnsParentWorkbook() {
        final ExtendedWorkbook parentExtendedWorkbook = PoiTestData.createExtendedWorkbook();
        final ExtendedSheet extendedSheet = (ExtendedSheet) parentExtendedWorkbook.createSheet();
        final ExtendedCell extendedCell = (ExtendedCell) extendedSheet.addRow().createCell(0);

        final ExtendedWorkbook returnedExtendedWorkbook = extendedCell.getWorkbook();

        Assertions.assertSame(parentExtendedWorkbook, returnedExtendedWorkbook);
    }

    // endregion

}
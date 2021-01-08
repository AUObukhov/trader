package ru.obukhov.investor.util.poi;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static ru.obukhov.investor.util.AssertUtils.assertCellAttributes;
import static ru.obukhov.investor.util.AssertUtils.assertRowValues;

public class ExtendedRowTest {

    // region constructor tests

    @Test(expected = IllegalArgumentException.class)
    public void constructor_throwsIllegalArgumentException_whenSheetIsNull() {
        Sheet sheet = ExcelTestDataHelper.createXSSFSheet();
        Row row = sheet.createRow(0);

        new ExtendedRow(null, row);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_throwsIllegalArgumentException_whenDelegateIsNull() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();

        new ExtendedRow(extendedSheet, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_throwsIllegalArgumentException_whenDelegateIsExtendedRow() {
        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        ExtendedRow extendedRow = extendedSheet.addRow();

        new ExtendedRow(extendedSheet, extendedRow);
    }

    @Test
    public void constructor_CopiesCells() {
        Sheet sheet = ExcelTestDataHelper.createXSSFSheet();
        Row row = sheet.createRow(0);

        String value0 = "cell0";
        String value1 = "cell1";
        ExcelTestDataHelper.createCell(row, 0, value0);
        ExcelTestDataHelper.createCell(row, 1, value1);

        ExtendedSheet extendedSheet = ExcelTestDataHelper.createExtendedSheet();
        ExtendedRow extendedRow = new ExtendedRow(extendedSheet, row);

        assertRowValues(extendedRow, value0, value1);
    }

    // endregion

    // region createCells tests

    @Test
    public void createCells_withNoColumn() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        Object[] values = {
                null,
                "value",
                BigDecimal.TEN,
                10,
                OffsetDateTime.now(),
                OffsetDateTime.now().toLocalDateTime()
        };

        List<ExtendedCell> cells = extendedRow.createCells(values);

        assertEquals(values.length, cells.size());

        assertCellAttributes(cells.get(0),
                extendedRow,
                0,
                CellType.BLANK,
                ExtendedWorkbook.CellStylesNames.NO_NAME_PREFIX + "1",
                values[0]);

        assertCellAttributes(cells.get(1),
                extendedRow,
                1,
                CellType.STRING,
                ExtendedWorkbook.CellStylesNames.STRING,
                values[1]);

        assertCellAttributes(cells.get(2),
                extendedRow,
                2,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                values[2]);

        assertCellAttributes(cells.get(3),
                extendedRow,
                3,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                values[3]);

        assertCellAttributes(cells.get(4),
                extendedRow,
                4,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                values[4]);

        assertCellAttributes(cells.get(5),
                extendedRow,
                5,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                values[5]);
    }

    @Test
    public void createCells_withColumn() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        Object[] values = {
                null,
                "value",
                BigDecimal.TEN,
                10,
                OffsetDateTime.now(),
                OffsetDateTime.now().toLocalDateTime()
        };

        List<ExtendedCell> cells = extendedRow.createCells(column, values);

        assertEquals(values.length, cells.size());

        assertCellAttributes(cells.get(0),
                extendedRow,
                column,
                CellType.BLANK,
                ExtendedWorkbook.CellStylesNames.NO_NAME_PREFIX + "1",
                values[0]);

        assertCellAttributes(cells.get(1),
                extendedRow,
                column + 1,
                CellType.STRING,
                ExtendedWorkbook.CellStylesNames.STRING,
                values[1]);

        assertCellAttributes(cells.get(2),
                extendedRow,
                column + 2,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                values[2]);

        assertCellAttributes(cells.get(3),
                extendedRow,
                column + 3,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                values[3]);

        assertCellAttributes(cells.get(4),
                extendedRow,
                column + 4,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                values[4]);

        assertCellAttributes(cells.get(5),
                extendedRow,
                column + 5,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                values[5]);
    }

    // endregion

    // region createCell with Object value tests

    @Test
    public void createCell_withObjectValue_whenValueIsNull() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        Object value = null;

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.BLANK,
                ExtendedWorkbook.CellStylesNames.NO_NAME_PREFIX + "1",
                value);
    }

    @Test
    public void createCell_withObjectValue_whenValueIsString() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        Object value = "value";

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.STRING,
                ExtendedWorkbook.CellStylesNames.STRING,
                value);
    }

    @Test
    public void createCell_withObjectValue_whenValueIsBigDecimal() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        Object value = BigDecimal.TEN;

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value);
    }

    @Test
    public void createCell_withObjectValue_whenValueIsInteger() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        Object value = 10;

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value);
    }

    @Test
    public void createCell_withObjectValue_whenValueIsLocalDateTime() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        Object value = offsetDateTime.toLocalDateTime();

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                value);
    }

    @Test
    public void createCell_withObjectValue_whenValueIsOffsetDateTime() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        Object value = OffsetDateTime.now();

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                value);
    }

    // endregion

    // region createCell with String value tests

    @Test
    public void createCell_withStringValue_whenValueIsNull() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        String value = null;

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.STRING,
                ExtendedWorkbook.CellStylesNames.STRING,
                value);
    }

    @Test
    public void createCell_withStringValue_whenValueIsNotNull() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        String value = "value";

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.STRING,
                ExtendedWorkbook.CellStylesNames.STRING,
                value);
    }

    // endregion

    // region createCell with BigDecimal value tests

    @Test
    public void createCell_withBigDecimalValue_whenValueIsNull() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        BigDecimal value = null;

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value);
    }

    @Test
    public void createCell_withBigDecimalValue_whenValueIsNotNull() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        BigDecimal value = BigDecimal.TEN;

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value);
    }

    // endregion

    // region createCell with Double value tests

    @Test
    public void createCell_withDoubleValue_whenValueIsNull() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        Double value = null;

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value);
    }

    @Test
    public void createCell_withDoubleValue_whenValueIsNotNull() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        Double value = 10d;

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value);
    }

    // endregion

    // region createCell with Integer value tests

    @Test
    public void createCell_withIntegerValue_whenValueIsNull() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        Integer value = null;

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value);
    }

    @Test
    public void createCell_withIntegerValue_whenValueIsNotNull() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        Integer value = 10;

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value);
    }

    // endregion

    // region createCell with LocalDateTime value tests

    @Test
    public void createCell_withLocalDateTimeValue_whenValueIsNull() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        LocalDateTime value = null;

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                value);
    }

    @Test
    public void createCell_withLocalDateTimeValue_whenValueIsNotNull() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        LocalDateTime value = offsetDateTime.toLocalDateTime();

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                value);
    }

    // endregion

    // region createCell with OffsetDateTime value tests

    @Test
    public void createCell_withOffsetDateTimeValue_whenValueIsNull() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        OffsetDateTime value = null;

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                value);
    }

    @Test
    public void createCell_withOffsetDateTimeValue_whenValueIsNotNull() {
        ExtendedRow extendedRow = ExcelTestDataHelper.createExtendedRow();
        int column = 5;
        OffsetDateTime value = OffsetDateTime.now();

        ExtendedCell cell = extendedRow.createCell(column, value);

        assertCellAttributes(cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                value);
    }

    // endregion

    // region getWorkbook tests

    @Test
    public void getWorkbook_returnsParentWorkbook() {
        ExtendedWorkbook parentExtendedWorkbook = ExcelTestDataHelper.createExtendedWorkbook();
        ExtendedRow extendedRow = (ExtendedRow) parentExtendedWorkbook.createSheet().createRow(0);

        ExtendedWorkbook returnedExtendedWorkbook = extendedRow.getWorkbook();

        assertSame(parentExtendedWorkbook, returnedExtendedWorkbook);
    }

    // endregion

}
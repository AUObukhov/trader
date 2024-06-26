package ru.obukhov.trader.common.model.poi;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.PoiTestData;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

class ExtendedRowUnitTest {

    // region constructor tests

    @Test
    void constructor_throwsIllegalArgumentException_whenSheetIsNull() throws IOException {
        final Sheet sheet = PoiTestData.createXSSFSheet();
        final Row row = sheet.createRow(0);

        final Executable executable = () -> new ExtendedRow(null, row);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "sheet can't be null");
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenDelegateIsNull() throws IOException {
        final ExtendedSheet extendedSheet = PoiTestData.createExtendedSheet();

        final Executable executable = () -> new ExtendedRow(extendedSheet, null);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "delegate can't be null");
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenDelegateIsExtendedRow() throws IOException {
        final ExtendedSheet extendedSheet = PoiTestData.createExtendedSheet();
        final ExtendedRow extendedRow = extendedSheet.addRow();

        final Executable executable = () -> new ExtendedRow(extendedSheet, extendedRow);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "delegate can't be ExtendedRow");
    }

    @Test
    void constructor_CopiesCells() throws IOException {
        final Sheet sheet = PoiTestData.createXSSFSheet();
        final Row row = sheet.createRow(0);

        final String value0 = "cell0";
        final String value1 = "cell1";
        PoiTestData.createCell(row, 0, value0);
        PoiTestData.createCell(row, 1, value1);

        final ExtendedSheet extendedSheet = PoiTestData.createExtendedSheet();
        final ExtendedRow extendedRow = new ExtendedRow(extendedSheet, row);

        AssertUtils.assertRowValues(extendedRow, value0, value1);
    }

    // endregion

    // region createCells tests

    @Test
    void createCells_withNoColumn() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final Object[] values = {
                null,
                "value",
                BigDecimal.TEN,
                10,
                DateUtils.now(),
                DateUtils.toLocalDateTime(DateUtils.now())
        };

        final List<ExtendedCell> cells = extendedRow.createCells(values);

        Assertions.assertEquals(values.length, cells.size());

        AssertUtils.assertCellAttributes(
                cells.getFirst(),
                extendedRow,
                0,
                CellType.BLANK,
                ExtendedWorkbook.CellStylesNames.NO_NAME_PREFIX + "1",
                values[0]
        );

        AssertUtils.assertCellAttributes(
                cells.get(1),
                extendedRow,
                1,
                CellType.STRING,
                ExtendedWorkbook.CellStylesNames.STRING,
                values[1]
        );

        AssertUtils.assertCellAttributes(
                cells.get(2),
                extendedRow,
                2,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                values[2]
        );

        AssertUtils.assertCellAttributes(
                cells.get(3),
                extendedRow,
                3,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                values[3]
        );

        AssertUtils.assertCellAttributes(
                cells.get(4),
                extendedRow,
                4,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                values[4]
        );

        AssertUtils.assertCellAttributes(
                cells.get(5),
                extendedRow,
                5,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                values[5]
        );
    }

    @Test
    void createCells_withColumn() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object[] values = {
                null,
                "value",
                BigDecimal.TEN,
                10,
                DateUtils.now(),
                DateUtils.toLocalDateTime(DateUtils.now())
        };

        final List<ExtendedCell> cells = extendedRow.createCells(column, values);

        Assertions.assertEquals(values.length, cells.size());

        AssertUtils.assertCellAttributes(
                cells.getFirst(),
                extendedRow,
                column,
                CellType.BLANK,
                ExtendedWorkbook.CellStylesNames.NO_NAME_PREFIX + "1",
                values[0]
        );

        AssertUtils.assertCellAttributes(
                cells.get(1),
                extendedRow,
                column + 1,
                CellType.STRING,
                ExtendedWorkbook.CellStylesNames.STRING,
                values[1]
        );

        AssertUtils.assertCellAttributes(
                cells.get(2),
                extendedRow,
                column + 2,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                values[2]
        );

        AssertUtils.assertCellAttributes(
                cells.get(3),
                extendedRow,
                column + 3,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                values[3]
        );

        AssertUtils.assertCellAttributes(
                cells.get(4),
                extendedRow,
                column + 4,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                values[4]
        );

        AssertUtils.assertCellAttributes(
                cells.get(5),
                extendedRow,
                column + 5,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                values[5]
        );
    }

    // endregion

    // region createUnitedCell value tests

    @Test
    void createUnitedCell_createsMergedRegion() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = "value";
        final int width = 4;

        extendedRow.createUnitedCell(column, value, width);

        final List<CellRangeAddress> mergedRegions = extendedRow.getSheet().getMergedRegions();
        Assertions.assertEquals(1, mergedRegions.size());
        final CellRangeAddress mergedRegion = mergedRegions.getFirst();
        Assertions.assertEquals(extendedRow.getRowNum(), mergedRegion.getFirstRow());
        Assertions.assertEquals(extendedRow.getRowNum(), mergedRegion.getLastRow());
        Assertions.assertEquals(column, mergedRegion.getFirstColumn());
        Assertions.assertEquals(column + width - 1, mergedRegion.getLastColumn());
    }

    // endregion

    // region createCell with Object value tests

    @Test
    void createCell_withObjectValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.BLANK,
                ExtendedWorkbook.CellStylesNames.NO_NAME_PREFIX + "1",
                value
        );
    }

    @Test
    void createCell_withObjectValue_whenValueIsString() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = "value";

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.STRING,
                ExtendedWorkbook.CellStylesNames.STRING,
                value
        );
    }

    @Test
    void createCell_withObjectValue_whenValueIsBigDecimal() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = BigDecimal.TEN;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value
        );
    }

    @Test
    void createCell_withObjectValue_whenValueIsInteger() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = 10;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value
        );
    }

    @Test
    void createCell_withObjectValue_whenValueIsLocalDateTime() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final OffsetDateTime offsetDateTime = DateUtils.now();
        final Object value = DateUtils.toLocalDateTime(offsetDateTime);

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                value
        );
    }

    @Test
    void createCell_withObjectValue_whenValueIsOffsetDateTime() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = DateUtils.now();

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                value
        );
    }

    // endregion

    // region createCell with String value tests

    @Test
    void createCell_withStringValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final String value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.STRING,
                ExtendedWorkbook.CellStylesNames.STRING,
                value
        );
    }

    @Test
    void createCell_withStringValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final String value = "value";

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.STRING,
                ExtendedWorkbook.CellStylesNames.STRING,
                value
        );
    }

    // endregion

    // region createCell with BigDecimal value tests

    @Test
    void createCell_withBigDecimalValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final BigDecimal value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value
        );
    }

    @Test
    void createCell_withBigDecimalValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final BigDecimal value = BigDecimal.TEN;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value
        );
    }

    // endregion

    // region createCell with Double value tests

    @Test
    void createCell_withDoubleValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Double value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value
        );
    }

    @Test
    void createCell_withDoubleValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Double value = 10d;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value
        );
    }

    // endregion

    // region createCell with Integer value tests

    @Test
    void createCell_withIntegerValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Integer value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value
        );
    }

    @Test
    void createCell_withIntegerValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Integer value = 10;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.NUMERIC,
                value
        );
    }

    // endregion

    // region createCell with LocalDateTime value tests

    @Test
    void createCell_withLocalDateTimeValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final LocalDateTime value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                value
        );
    }

    @Test
    void createCell_withLocalDateTimeValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final OffsetDateTime offsetDateTime = DateUtils.now();
        final LocalDateTime value = DateUtils.toLocalDateTime(offsetDateTime);

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                value
        );
    }

    // endregion

    // region createCell with OffsetDateTime value tests

    @Test
    void createCell_withOffsetDateTimeValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final OffsetDateTime value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                value
        );
    }

    @Test
    void createCell_withOffsetDateTimeValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final OffsetDateTime value = DateUtils.now();

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.NUMERIC,
                ExtendedWorkbook.CellStylesNames.DATE_TIME,
                value
        );
    }

    // endregion

    // region createCell with Boolean value tests

    @Test
    void createCell_withBooleanValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Boolean value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.BOOLEAN,
                ExtendedWorkbook.CellStylesNames.BOOLEAN,
                value
        );
    }

    @Test
    void createCell_withBooleanValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Boolean value = true;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        AssertUtils.assertCellAttributes(
                cell,
                extendedRow,
                column,
                CellType.BOOLEAN,
                ExtendedWorkbook.CellStylesNames.BOOLEAN,
                value
        );
    }

    // endregion

    // region getWorkbook tests

    @Test
    void getWorkbook_returnsParentWorkbook() {
        final ExtendedWorkbook parentExtendedWorkbook = PoiTestData.createExtendedWorkbook();
        final ExtendedRow extendedRow = (ExtendedRow) parentExtendedWorkbook.createSheet().createRow(0);

        final ExtendedWorkbook returnedExtendedWorkbook = extendedRow.getWorkbook();

        Assertions.assertSame(parentExtendedWorkbook, returnedExtendedWorkbook);
    }

    // endregion

}
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

        final String expectedCellStyle0 = ExtendedWorkbook.CellStylesNames.NO_NAME_PREFIX + "1";
        AssertUtils.assertCell(cells.getFirst(), extendedRow, 0, CellType.BLANK, expectedCellStyle0, values[0]);

        final String expectedCellStyle1 = ExtendedWorkbook.CellStylesNames.STRING;
        AssertUtils.assertCell(cells.get(1), extendedRow, 1, CellType.STRING, expectedCellStyle1, values[1]);

        final String expectedCellStyle2 = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cells.get(2), extendedRow, 2, CellType.NUMERIC, expectedCellStyle2, values[2]);

        final String expectedCellStyle3 = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cells.get(3), extendedRow, 3, CellType.NUMERIC, expectedCellStyle3, values[3]);

        final String expectedCellStyle4 = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        AssertUtils.assertCell(cells.get(4), extendedRow, 4, CellType.NUMERIC, expectedCellStyle4, values[4]);

        final String expectedCellStyle5 = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        AssertUtils.assertCell(cells.get(5), extendedRow, 5, CellType.NUMERIC, expectedCellStyle5, values[5]);
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

        final String expectedCellStyle0 = ExtendedWorkbook.CellStylesNames.NO_NAME_PREFIX + "1";
        AssertUtils.assertCell(cells.get(0), extendedRow, column, CellType.BLANK, expectedCellStyle0, values[0]);

        final String expectedCellStyle1 = ExtendedWorkbook.CellStylesNames.STRING;
        final int column1 = column + 1;
        AssertUtils.assertCell(cells.get(1), extendedRow, column1, CellType.STRING, expectedCellStyle1, values[1]);

        final String expectedCellStyle2 = ExtendedWorkbook.CellStylesNames.NUMERIC;
        final int column2 = column + 2;
        AssertUtils.assertCell(cells.get(2), extendedRow, column2, CellType.NUMERIC, expectedCellStyle2, values[2]);

        final String expectedCellStyle3 = ExtendedWorkbook.CellStylesNames.NUMERIC;
        final int column3 = column + 3;
        AssertUtils.assertCell(cells.get(3), extendedRow, column3, CellType.NUMERIC, expectedCellStyle3, values[3]);

        final String expectedCellStyle4 = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        final int column4 = column + 4;
        AssertUtils.assertCell(cells.get(4), extendedRow, column4, CellType.NUMERIC, expectedCellStyle4, values[4]);

        final String expectedCellStyle5 = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        final int column5 = column + 5;
        AssertUtils.assertCell(cells.get(5), extendedRow, column5, CellType.NUMERIC, expectedCellStyle5, values[5]);
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

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NO_NAME_PREFIX + "1";
        AssertUtils.assertCell(cell, extendedRow, column, CellType.BLANK, expectedCellStyle, value);
    }

    @Test
    void createCell_withObjectValue_whenValueIsString() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = "value";

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.STRING;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.STRING, expectedCellStyle, value);
    }

    @Test
    void createCell_withObjectValue_whenValueIsBigDecimal() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = BigDecimal.TEN;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withObjectValue_whenValueIsInteger() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = 10;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withObjectValue_whenValueIsLocalDateTime() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final OffsetDateTime offsetDateTime = DateUtils.now();
        final Object value = DateUtils.toLocalDateTime(offsetDateTime);

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withObjectValue_whenValueIsOffsetDateTime() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = DateUtils.now();

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    // endregion

    // region createFormulaCell

    @Test
    void createFormulaCell_noCellStyle() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final String value = "A1+A2";

        final ExtendedCell cell = extendedRow.createFormulaCell(column, value);

        String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.FORMULA, expectedCellStyle, value);
    }

    @Test
    void createFormulaCell_withCellStyle() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final String value = "A1+A2";
        final String cellStyle = ExtendedWorkbook.CellStylesNames.STRING;

        final ExtendedCell cell = extendedRow.createFormulaCell(column, value, cellStyle);

        AssertUtils.assertCell(cell, extendedRow, column, CellType.FORMULA, cellStyle, value);
    }

    // endregion

    // region createCell with String value tests

    @Test
    void createCell_withStringValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final String value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.STRING;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.STRING, expectedCellStyle, value);
    }

    @Test
    void createCell_withStringValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final String value = "value";

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.STRING;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.STRING, expectedCellStyle, value);
    }

    // endregion

    // region createCell with BigDecimal value tests

    @Test
    void createCell_withBigDecimalValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final BigDecimal value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withBigDecimalValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final BigDecimal value = BigDecimal.TEN;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    // endregion

    // region createCell with BigDecimal and cellStyle value tests

    @Test
    void createCell_withBigDecimalValue_andCellStyle_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final BigDecimal value = null;
        final String cellStyle = ExtendedWorkbook.CellStylesNames.PERCENT;

        final ExtendedCell cell = extendedRow.createCell(column, value, cellStyle);

        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, cellStyle, value);
    }

    @Test
    void createCell_withBigDecimalValue_andCellStyle_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final BigDecimal value = BigDecimal.TEN;
        final String cellStyle = ExtendedWorkbook.CellStylesNames.PERCENT;

        final ExtendedCell cell = extendedRow.createCell(column, value, cellStyle);

        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, cellStyle, value);
    }

    // endregion

    // region createCell with Double value tests

    @Test
    void createCell_withDoubleValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Double value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withDoubleValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Double value = 10d;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    // endregion

    // region createCell with Double value tests

    @Test
    void createCell_withDoubleValue_andCellStyle_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Double value = null;
        final String cellStyle = ExtendedWorkbook.CellStylesNames.PERCENT;

        final ExtendedCell cell = extendedRow.createCell(column, value, cellStyle);

        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, cellStyle, value);
    }

    @Test
    void createCell_withDoubleValue_andCellStyle_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Double value = 10d;
        final String cellStyle = ExtendedWorkbook.CellStylesNames.PERCENT;

        final ExtendedCell cell = extendedRow.createCell(column, value, cellStyle);

        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, cellStyle, value);
    }

    // endregion

    // region createCell with Integer value tests

    @Test
    void createCell_withIntegerValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Integer value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withIntegerValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Integer value = 10;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    // endregion

    // region createCell with LocalDateTime value tests

    @Test
    void createCell_withLocalDateTimeValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final LocalDateTime value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withLocalDateTimeValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final OffsetDateTime offsetDateTime = DateUtils.now();
        final LocalDateTime value = DateUtils.toLocalDateTime(offsetDateTime);

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    // endregion

    // region createCell with OffsetDateTime value tests

    @Test
    void createCell_withOffsetDateTimeValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final OffsetDateTime value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withOffsetDateTimeValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final OffsetDateTime value = DateUtils.now();

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    // endregion

    // region createCell with Boolean value tests

    @Test
    void createCell_withBooleanValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Boolean value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.BOOLEAN;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.BOOLEAN, expectedCellStyle, value);
    }

    @Test
    void createCell_withBooleanValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Boolean value = true;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.BOOLEAN;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.BOOLEAN, expectedCellStyle, value);
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
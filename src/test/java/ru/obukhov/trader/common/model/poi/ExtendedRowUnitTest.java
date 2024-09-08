package ru.obukhov.trader.common.model.poi;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.PoiTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.core.models.Money;

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
        final String expectedMessage = "delegate can't be null";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenDelegateIsExtendedRow() throws IOException {
        final ExtendedSheet extendedSheet = PoiTestData.createExtendedSheet();
        final ExtendedRow extendedRow = extendedSheet.addRow();

        final Executable executable = () -> new ExtendedRow(extendedSheet, extendedRow);
        final String expectedMessage = "delegate can't be ExtendedRow";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
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

    // region createUnitedCell tests

    @Test
    void createUnitedCell_noColumn_createsMergedRegion() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final Object value = "value";
        final int width = 4;

        extendedRow.createUnitedCell(value, width);

        final List<CellRangeAddress> mergedRegions = extendedRow.getSheet().getMergedRegions();
        Assertions.assertEquals(1, mergedRegions.size());
        final CellRangeAddress mergedRegion = mergedRegions.getFirst();
        Assertions.assertEquals(extendedRow.getRowNum(), mergedRegion.getFirstRow());
        Assertions.assertEquals(extendedRow.getRowNum(), mergedRegion.getLastRow());
        Assertions.assertEquals(0, mergedRegion.getFirstColumn());
        Assertions.assertEquals(width - 1, mergedRegion.getLastColumn());
    }

    @Test
    void createUnitedCell_withColumn_createsMergedRegion() throws IOException {
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
    void createCell_withObjectValue_whenValueIsMoney() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final double numericValue = 100.25;
        final Object value = TestData.newMoney(numericValue, Currencies.RUB);

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, numericValue);
    }

    @Test
    void createCell_withObjectValue_whenValueIsMoneyValue() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final double numericValue = 100.25;
        final Object value = TestData.newMoneyValue(numericValue, Currencies.RUB);

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, numericValue);
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
    void createCell_withObjectValue_whenValueIsDouble() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = 100.25;

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
    void createCell_withObjectValue_whenValueIsLong() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = 10L;

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

    @Test
    void createCell_withObjectValue_whenValueIsTimestamp() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = DateTimeTestData.newTimestamp(1725767978);

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withObjectValue_whenValueIsBoolean() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = true;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.BOOLEAN;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.BOOLEAN, expectedCellStyle, value);
    }

    @Test
    void createCell_withObjectValue_throwsIllegalArgumentException_whenValueHasUnsupportedType() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Object value = List.of();

        final Executable executable = () -> extendedRow.createCell(column, value);
        final String expectedMessage = "Unexpected type of value: " + value.getClass();
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
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

    // region createCell with String tests

    @Test
    void createCell_withString_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final String value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.STRING;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.STRING, expectedCellStyle, value);
    }

    @Test
    void createCell_withString_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final String value = "value";

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.STRING;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.STRING, expectedCellStyle, value);
    }

    // endregion

    // region createCell with Money tests

    @Test
    void createCell_withMoney_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Money value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withMoney_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final double numericValue = 100.25;
        final Money value = TestData.newMoney(numericValue, Currencies.RUB);

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, numericValue);
    }

    // endregion

    // region createCell with MoneyValue tests

    @Test
    void createCell_withMoneyValue_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final MoneyValue value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withMoneyValue_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final double numericValue = 100.25;
        final MoneyValue value = TestData.newMoneyValue(numericValue, Currencies.RUB);

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, numericValue);
    }

    // endregion

    // region createCell with BigDecimal tests

    @Test
    void createCell_withBigDecimal_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final BigDecimal value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withBigDecimal_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final BigDecimal value = BigDecimal.TEN;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    // endregion

    // region createCell with BigDecimal and cellStyle tests

    @Test
    void createCell_withBigDecimal_andCellStyle_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final BigDecimal value = null;
        final String cellStyle = ExtendedWorkbook.CellStylesNames.PERCENT;

        final ExtendedCell cell = extendedRow.createCell(column, value, cellStyle);

        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, cellStyle, value);
    }

    @Test
    void createCell_withBigDecimal_andCellStyle_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final BigDecimal value = BigDecimal.TEN;
        final String cellStyle = ExtendedWorkbook.CellStylesNames.PERCENT;

        final ExtendedCell cell = extendedRow.createCell(column, value, cellStyle);

        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, cellStyle, value);
    }

    // endregion

    // region createCell with Double tests

    @Test
    void createCell_withDouble_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Double value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withDouble_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Double value = 10d;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    // endregion

    // region createCell with Double and CellStyle tests

    @Test
    void createCell_withDouble_andCellStyle_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Double value = null;
        final String cellStyle = ExtendedWorkbook.CellStylesNames.PERCENT;

        final ExtendedCell cell = extendedRow.createCell(column, value, cellStyle);

        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, cellStyle, value);
    }

    @Test
    void createCell_withDouble_andCellStyle_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Double value = 10d;
        final String cellStyle = ExtendedWorkbook.CellStylesNames.PERCENT;

        final ExtendedCell cell = extendedRow.createCell(column, value, cellStyle);

        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, cellStyle, value);
    }

    // endregion

    // region createCell with Integer tests

    @Test
    void createCell_withInteger_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Integer value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withInteger_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Integer value = 10;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    // endregion

    // region createCell with Long tests

    @Test
    void createCell_withLong_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Long value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withLong_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Long value = 10L;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.NUMERIC;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    // endregion

    // region createCell with LocalDateTime tests

    @Test
    void createCell_withLocalDateTime_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final LocalDateTime value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withLocalDateTime_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final OffsetDateTime offsetDateTime = DateUtils.now();
        final LocalDateTime value = DateUtils.toLocalDateTime(offsetDateTime);

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    // endregion

    // region createCell with OffsetDateTime tests

    @Test
    void createCell_withOffsetDateTime_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final OffsetDateTime value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    @Test
    void createCell_withOffsetDateTime_whenValueIsNotNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final OffsetDateTime value = DateUtils.now();

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.DATE_TIME;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.NUMERIC, expectedCellStyle, value);
    }

    // endregion

    // region createCell with Boolean tests

    @Test
    void createCell_withBoolean_whenValueIsNull() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final Boolean value = null;

        final ExtendedCell cell = extendedRow.createCell(column, value);

        final String expectedCellStyle = ExtendedWorkbook.CellStylesNames.BOOLEAN;
        AssertUtils.assertCell(cell, extendedRow, column, CellType.BOOLEAN, expectedCellStyle, value);
    }

    @Test
    void createCell_withBoolean_whenValueIsNotNull() throws IOException {
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

    // region getCell no MissingCellPolicy tests

    @Test
    void getCell_noMissingCellPolicy_whenCellExists() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final String value = "value";
        final ExtendedCell cell = extendedRow.createCell(column, value);

        final ExtendedCell returnedCell = extendedRow.getCell(column);

        Assertions.assertSame(cell, returnedCell);
    }

    @Test
    void getCell_noMissingCellPolicy_whenCellNotExists() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final String value = "value";
        extendedRow.createCell(column, value);

        final ExtendedCell returnedCell = extendedRow.getCell(column - 1);

        Assertions.assertNull(returnedCell);
    }

    // endregion

    // region getCell with MissingCellPolicy.RETURN_NULL_AND_BLANK tests

    @Test
    void getCell_withMissingCellPolicyReturnNullAndBlank_whenCellExists() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final String value = "value";
        final ExtendedCell cell = extendedRow.createCell(column, value);

        final ExtendedCell returnedCell = extendedRow.getCell(column, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);

        Assertions.assertSame(cell, returnedCell);
    }

    @Test
    void getCell_withMissingCellPolicyReturnNullAndBlank_whenCellNotExists() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final String value = "value";
        extendedRow.createCell(column, value);

        final ExtendedCell returnedCell = extendedRow.getCell(column - 1, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);

        Assertions.assertNull(returnedCell);
    }

    // endregion

    // region getCell with MissingCellPolicy.RETURN_BLANK_AS_NULL tests

    @Test
    void getCell_withMissingCellPolicyReturnBlankAsNull_whenCellNotExists() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        extendedRow.createCell(column);

        final ExtendedCell returnedCell = extendedRow.getCell(column - 1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        Assertions.assertNull(returnedCell);
    }

    @Test
    void getCell_withMissingCellPolicyReturnBlankAsNull_whenCellExistsAndBlank() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        extendedRow.createCell(column);

        final ExtendedCell returnedCell = extendedRow.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        Assertions.assertNull(returnedCell);
    }

    @Test
    void getCell_withMissingCellPolicyReturnBlankAsNull_whenCellExistsAndNotBlank() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final String value = "value";
        final ExtendedCell cell = extendedRow.createCell(column, value);

        final ExtendedCell returnedCell = extendedRow.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        Assertions.assertSame(cell, returnedCell);
    }

    // endregion

    // region getCell with MissingCellPolicy.CREATE_NULL_AS_BLANK tests

    @Test
    void getCell_withMissingCellPolicyCreateNullAsBlank_whenCellNotExists() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        extendedRow.createCell(column);

        final ExtendedCell returnedCell = extendedRow.getCell(column - 1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        Assertions.assertEquals(CellType.BLANK, returnedCell.getCellType());
    }

    @Test
    void getCell_withMissingCellPolicyCreateNullAsBlank_whenCellExists() throws IOException {
        final ExtendedRow extendedRow = PoiTestData.createExtendedRow();
        final int column = 5;
        final String value = "value";
        final ExtendedCell cell = extendedRow.createCell(column, value);

        final ExtendedCell returnedCell = extendedRow.getCell(column, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        Assertions.assertSame(cell, returnedCell);
    }

    // endregion

}
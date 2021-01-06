package ru.obukhov.investor.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.junit.Assert;
import ru.obukhov.investor.util.poi.ExtendedCell;
import ru.obukhov.investor.util.poi.ExtendedRow;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class AssertUtils {

    public static void assertEquals(double expected, double actual) {
        org.junit.Assert.assertEquals(expected, actual, NumberUtils.DOUBLE_ZERO);
    }

    public static void assertRowValues(Row row, Object... values) {
        Assert.assertEquals(values.length, row.getPhysicalNumberOfCells());
        for (int index = 0; index < values.length; index++) {
            assertCellValue(row.getCell(index), values[index]);
        }
    }

    public static void assertCellAttributes(ExtendedCell cell,
                                            ExtendedRow extendedRow,
                                            int column,
                                            CellType cellType,
                                            String cellStyleName,
                                            Object value) {

        Assert.assertSame(extendedRow, cell.getRow());
        Assert.assertEquals(column, cell.getColumnIndex());
        Assert.assertEquals(cellType, cell.getCellType());

        CellStyle expectedCellStyle = extendedRow.getWorkbook().getCellStyle(cellStyleName);
        Assert.assertEquals(expectedCellStyle, cell.getCellStyle());

        assertCellValue(cell, value);
    }

    public static void assertCellValue(Cell cell, Object value) {
        switch (cell.getCellType()) {
            case BLANK:
                Assert.assertEquals(StringUtils.EMPTY, cell.getStringCellValue());
                AssertUtils.assertEquals(NumberUtils.DOUBLE_ZERO, cell.getNumericCellValue());
                Assert.assertNull(cell.getDateCellValue());
                break;

            case STRING:
                assertCellValue(cell, (String) value);
                break;

            case NUMERIC:
                if (value == null) {
                    AssertUtils.assertEquals(NumberUtils.DOUBLE_ZERO, cell.getNumericCellValue());
                } else if (value instanceof BigDecimal) {
                    assertCellValue(cell, (BigDecimal) value);
                } else if (value instanceof Integer) {
                    assertCellValue(cell, (Integer) value);
                } else if (value instanceof LocalDateTime) {
                    assetCellValue(cell, (LocalDateTime) value);
                } else if (value instanceof OffsetDateTime) {
                    assertCellValue(cell, (OffsetDateTime) value);
                } else {
                    throw new IllegalArgumentException("Unexpected value " + value);
                }
                break;

            default:
                throw new IllegalArgumentException("Unexpected cell type " + cell.getCellType());
        }
    }

    public static void assertCellValue(Cell cell, String value) {
        String exceptedValue = value == null ? StringUtils.EMPTY : value;
        Assert.assertEquals(exceptedValue, cell.getStringCellValue());
    }

    public static void assertCellValue(Cell cell, BigDecimal value) {
        double expectedValue = value == null ? NumberUtils.DOUBLE_ZERO : value.doubleValue();
        AssertUtils.assertEquals(expectedValue, cell.getNumericCellValue());
    }

    public static void assertCellValue(Cell cell, Integer value) {
        double expectedValue = value == null ? NumberUtils.DOUBLE_ZERO : value.doubleValue();
        AssertUtils.assertEquals(expectedValue, cell.getNumericCellValue());
    }

    private static void assetCellValue(Cell cell, LocalDateTime value) {
        ZoneOffset offset = OffsetDateTime.now().getOffset();
        Date expectedValue = Date.from(value.toInstant(offset));
        Assert.assertEquals(expectedValue, cell.getDateCellValue());
    }

    public static void assertCellValue(Cell cell, OffsetDateTime value) {
        Date expectedValue = Date.from(value.toInstant());
        Assert.assertEquals(expectedValue, cell.getDateCellValue());
    }

}
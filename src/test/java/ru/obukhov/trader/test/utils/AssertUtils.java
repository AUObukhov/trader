package ru.obukhov.trader.test.utils;

import com.google.common.base.Stopwatch;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.validation.ObjectError;
import ru.obukhov.trader.common.model.poi.ExtendedCell;
import ru.obukhov.trader.common.model.poi.ExtendedRow;
import ru.obukhov.trader.common.util.MathUtils;

import javax.validation.ConstraintViolation;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class AssertUtils {

    public static void assertEquals(double expected, double actual) {
        Assertions.assertEquals(expected, actual, NumberUtils.DOUBLE_ZERO);
    }

    public static void assertEquals(BigDecimal expected, BigDecimal actual) {
        if (!MathUtils.numbersEqual(actual, expected)) {
            String message = String.format("expected: <%s> but was: <%s>", expected, actual);
            Assertions.fail(message);
        }
    }

    public static void assertEquals(int expected, BigDecimal actual) {
        if (!MathUtils.numbersEqual(actual, expected)) {
            String message = String.format("expected: <%s> but was: <%s>", expected, actual);
            Assertions.fail(message);
        }
    }

    public static void assertEquals(double expected, BigDecimal actual) {
        if (!MathUtils.numbersEqual(actual, expected)) {
            String message = String.format("expected: <%s> but was: <%s>", expected, actual);
            Assertions.fail(message);
        }
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
                } else if (value instanceof Double) {
                    assertCellValue(cell, (Double) value);
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

    public static void assertCellValue(Cell cell, Double value) {
        double expectedValue = ObjectUtils.defaultIfNull(value, NumberUtils.DOUBLE_ZERO);
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

    public static void assertFaster(CheckedRunnable runnable, long time) throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        runnable.run();
        stopwatch.stop();
        long elapsed = stopwatch.elapsed().toMillis();
        if (elapsed >= time) {
            Assertions.fail("Expected execution faster than " + time + " ms. Actual is " + elapsed + " ms");
        }
    }

    public static void assertSlower(CheckedRunnable runnable, long time) throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        runnable.run();
        stopwatch.stop();
        long elapsed = stopwatch.elapsed().toMillis();
        if (elapsed < time) {
            Assertions.fail("Expected execution slower than " + time + " ms. Actual is " + elapsed + " ms");
        }
    }

    public static ContextConsumer<AssertableApplicationContext> createContextFailureAssertConsumer(String message) {
        return context -> AssertUtils.assertContextStartupFailedWithMessage(context, message);
    }

    public static void assertContextStartupFailedWithMessage(AssertableApplicationContext context, String message) {
        Throwable startupFailure = context.getStartupFailure();

        Assertions.assertNotNull(startupFailure, "context startup not failed as expected");

        BindValidationException bindValidationException =
                (BindValidationException) startupFailure.getCause().getCause();
        List<ObjectError> errors = bindValidationException.getValidationErrors().getAllErrors();

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(message, errors.get(0).getDefaultMessage());
    }

    public static <T> void assertViolation(Set<ConstraintViolation<T>> violations, String expectedMessage) {
        Assertions.assertEquals(1, violations.size(), "expected single violation");
        Assertions.assertEquals(expectedMessage, violations.iterator().next().getMessage());
    }

    public static <T extends Throwable> void assertThrowsWithMessage(Executable executable,
                                                                     Class<T> expectedType,
                                                                     String expectedMessage) {

        Throwable throwable = Assertions.assertThrows(expectedType, executable);
        Assertions.assertEquals(expectedMessage, throwable.getMessage());
    }

}
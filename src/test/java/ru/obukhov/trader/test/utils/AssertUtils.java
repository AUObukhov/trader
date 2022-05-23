package ru.obukhov.trader.test.utils;

import com.google.protobuf.Timestamp;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xddf.usermodel.XDDFColorRgbBinary;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.mapstruct.factory.Mappers;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarker;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.validation.ObjectError;
import ru.obukhov.trader.common.model.poi.ColorMapper;
import ru.obukhov.trader.common.model.poi.ExtendedCell;
import ru.obukhov.trader.common.model.poi.ExtendedRow;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.ExecutionUtils;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.tinkoff.piapi.contract.v1.MoneyValue;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.awt.Color;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class AssertUtils {

    private static final ColorMapper COLOR_MAPPER = Mappers.getMapper(ColorMapper.class);
    private static final MoneyValueMapper MONEY_MAPPER = Mappers.getMapper(MoneyValueMapper.class);
    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    // region assertEquals

    public static void assertEquals(final double expected, final double actual) {
        Assertions.assertEquals(expected, actual, NumberUtils.DOUBLE_ZERO);
    }

    public static void assertEquals(final BigDecimal expected, final BigDecimal actual) {
        if (!DecimalUtils.numbersEqual(actual, expected)) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(@Nullable final Integer expected, final BigDecimal actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (!DecimalUtils.numbersEqual(actual, expected)) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(@Nullable final Long expected, final BigDecimal actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (!DecimalUtils.numbersEqual(actual, expected)) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(@Nullable final Double expected, final BigDecimal actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (!DecimalUtils.numbersEqual(actual, expected)) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(@Nullable final MoneyValue expected, final BigDecimal actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (!DecimalUtils.numbersEqual(actual, MONEY_MAPPER.map(expected))) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(@Nullable final BigDecimal expected, final MoneyValue actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (!DecimalUtils.numbersEqual(expected, MONEY_MAPPER.map(actual))) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(@Nullable final Timestamp expected, @Nullable final OffsetDateTime actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (!DATE_TIME_MAPPER.map(expected).equals(actual)) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(@Nullable final OffsetDateTime expected, @Nullable final Timestamp actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (!DATE_TIME_MAPPER.map(expected).equals(actual)) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(final byte[] expected, final byte[] actual) {
        Assertions.assertEquals(expected.length, actual.length);
        for (int i = 0; i < actual.length; i++) {
            Assertions.assertEquals(expected[i], actual[i]);
        }
    }

    public static <K, V> void assertEquals(final Map<? extends K, ? extends V> expected, final Map<? extends K, ? extends V> actual) {
        final Set<? extends K> actualKeys = actual.keySet();
        Assertions.assertEquals(expected.keySet(), actualKeys);
        for (K key : actualKeys) {
            Assertions.assertEquals(expected.get(key), actual.get(key));
        }
    }

    // endregion

    public static void assertRangeInclusive(final long expectedMin, final long expectedMax, final long actual) {
        Assertions.assertTrue(actual >= expectedMin);
        Assertions.assertTrue(actual <= expectedMax);
    }

    public static void assertMatchesRegex(final String value, final String regex) {
        final Matcher matcher = Pattern.compile(regex).matcher(value);
        if (!matcher.matches()) {
            Assertions.fail(value + System.lineSeparator() + "does not matches regex:" + System.lineSeparator() + regex);
        }
    }

    // region list assertions

    public static void assertEquals(final List<?> expected, final List<?> actual) {
        assertListSize(expected, actual);

        final StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < expected.size(); i++) {
            final Object expectedValue = expected.get(i);
            final Object actualValue = actual.get(i);
            if (expectedValue instanceof List expectedList && actualValue instanceof List actualList) {
                assertEquals(expectedList, actualList);
            } else if (expectedValue instanceof BigDecimal expectedBigDecimal && actualValue instanceof BigDecimal actualBigDecimal) {
                if (!DecimalUtils.numbersEqual(actualBigDecimal, expectedBigDecimal)) {
                    messageBuilder.append(getErrorMessage(expectedValue, actualValue, i))
                            .append(System.lineSeparator());
                }
            } else if (!Objects.equals(expectedValue, actualValue)) {
                messageBuilder.append(getErrorMessage(expectedValue, actualValue, i))
                        .append(System.lineSeparator());
            }
        }

        final String message = messageBuilder.toString();
        if (!message.isEmpty()) {
            Assertions.fail(message);
        }
    }

    private static String getErrorMessage(Object expectedValue, Object actualValue, int index) {
        return String.format("expected: <%s> at position <%s> but was: <%s>", expectedValue, index, actualValue);
    }

    private static void assertListSize(final List<?> expected, final List<?> actual) {
        if (expected.size() != actual.size()) {
            final String message = String.format("expected list of size: <%s> but was: <%s>", expected.size(), actual.size());
            Assertions.fail(message);
        }
    }

    // endregion

    public static <K, V> void assertMapsAreEqual(final Map<K, V> expected, final Map<K, V> actual) {
        Assertions.assertEquals(expected.getClass(), actual.getClass());
        Assertions.assertEquals(expected.size(), actual.size());

        final StringBuilder messageBuilder = new StringBuilder();
        for (K key : expected.keySet()) {
            final V expectedValue = expected.get(key);
            final V actualValue = actual.get(key);
            if (!Objects.equals(expectedValue, actualValue)) {
                messageBuilder.append(String.format("expected: <%s> at key <%s> but was: <%s>", expectedValue, key, actualValue))
                        .append(System.lineSeparator());
            }
        }

        final String message = messageBuilder.toString();
        if (!message.isEmpty()) {
            Assertions.fail(message);
        }
    }

    // region apache poi assertions

    public static void assertRowValues(final Row row, final Object... values) {
        Assertions.assertEquals(values.length, row.getPhysicalNumberOfCells());
        for (int index = 0; index < values.length; index++) {
            assertCellValue(row.getCell(index), values[index]);
        }
    }

    public static void assertCellAttributes(
            final ExtendedCell cell,
            final ExtendedRow extendedRow,
            final int column,
            final CellType cellType,
            final String cellStyleName,
            final Object value
    ) {
        Assertions.assertSame(extendedRow, cell.getRow());
        Assertions.assertEquals(column, cell.getColumnIndex());
        Assertions.assertEquals(cellType, cell.getCellType());

        final CellStyle expectedCellStyle = extendedRow.getWorkbook().getCellStyle(cellStyleName);
        Assertions.assertEquals(expectedCellStyle, cell.getCellStyle());

        assertCellValue(cell, value);
    }

    public static void assertCellValue(final Cell cell, final Object value) {
        switch (cell.getCellType()) {
            case BLANK:
                Assertions.assertEquals(StringUtils.EMPTY, cell.getStringCellValue());
                AssertUtils.assertEquals(NumberUtils.DOUBLE_ZERO, cell.getNumericCellValue());
                Assertions.assertNull(cell.getDateCellValue());
                break;

            case STRING:
                assertCellValue(cell, (String) value);
                break;

            case NUMERIC:
                if (value == null) {
                    AssertUtils.assertEquals(NumberUtils.DOUBLE_ZERO, cell.getNumericCellValue());
                } else if (value instanceof BigDecimal bigDecimalValue) {
                    assertCellValue(cell, bigDecimalValue);
                } else if (value instanceof Double doubleValue) {
                    assertCellValue(cell, doubleValue);
                } else if (value instanceof Integer integerValue) {
                    assertCellValue(cell, integerValue);
                } else if (value instanceof Long longValue) {
                    assertCellValue(cell, longValue);
                } else if (value instanceof LocalDateTime localDateTimeValue) {
                    assetCellValue(cell, localDateTimeValue);
                } else if (value instanceof OffsetDateTime offsetDateTimeValue) {
                    assertCellValue(cell, offsetDateTimeValue);
                } else {
                    throw new IllegalArgumentException("Unexpected value " + value);
                }
                break;

            case BOOLEAN:
                assertCellValue(cell, (Boolean) value);
                break;

            default:
                throw new IllegalArgumentException("Unexpected cell type " + cell.getCellType());
        }
    }

    public static void assertCellValue(final Cell cell, final String value) {
        final String exceptedValue = value == null ? StringUtils.EMPTY : value;
        Assertions.assertEquals(exceptedValue, cell.getStringCellValue());
    }

    public static void assertCellValue(final Cell cell, final BigDecimal value) {
        final double expectedValue = value == null ? NumberUtils.DOUBLE_ZERO : value.doubleValue();
        AssertUtils.assertEquals(expectedValue, cell.getNumericCellValue());
    }

    public static void assertCellValue(final Cell cell, final Double value) {
        final double expectedValue = ObjectUtils.defaultIfNull(value, NumberUtils.DOUBLE_ZERO);
        AssertUtils.assertEquals(expectedValue, cell.getNumericCellValue());
    }

    public static void assertCellValue(final Cell cell, final Integer value) {
        final double expectedValue = value == null ? NumberUtils.DOUBLE_ZERO : value.doubleValue();
        AssertUtils.assertEquals(expectedValue, cell.getNumericCellValue());
    }

    public static void assertCellValue(final Cell cell, final Long value) {
        final double expectedValue = value == null ? NumberUtils.DOUBLE_ZERO : value.doubleValue();
        AssertUtils.assertEquals(expectedValue, cell.getNumericCellValue());
    }

    public static void assertCellValue(final Cell cell, final Boolean value) {
        final boolean expectedValue = BooleanUtils.isTrue(value);
        Assertions.assertEquals(expectedValue, cell.getBooleanCellValue());
    }

    private static void assetCellValue(final Cell cell, final LocalDateTime value) {
        final ZoneOffset offset = OffsetDateTime.now().getOffset();
        final Date expectedValue = Date.from(value.toInstant(offset));
        Assertions.assertEquals(expectedValue, cell.getDateCellValue());
    }

    public static void assertCellValue(final Cell cell, final OffsetDateTime value) {
        final Date expectedValue = Date.from(value.toInstant());
        Assertions.assertEquals(expectedValue, cell.getDateCellValue());
    }

    public static void assertSeriesColor(final XDDFLineChartData.Series series, final Color color) {
        final XDDFSolidFillProperties fillProperties1 = (XDDFSolidFillProperties) series.getShapeProperties().getFillProperties();
        final XDDFColorRgbBinary xddfColor1 = (XDDFColorRgbBinary) fillProperties1.getColor();
        AssertUtils.assertEquals(COLOR_MAPPER.mapToBytes(color), xddfColor1.getValue());
    }

    @SneakyThrows
    public static void assertSeriesMarkerColor(final XDDFLineChartData.Series series, final Color color) {
        final Method getMarkerMethod = series.getClass().getDeclaredMethod("getMarker");
        getMarkerMethod.setAccessible(true);
        final CTMarker marker = (CTMarker) getMarkerMethod.invoke(series);
        final byte[] colorBytes = marker.getSpPr().getSolidFill().getSrgbClr().getVal();
        AssertUtils.assertEquals(COLOR_MAPPER.mapToBytes(color), colorBytes);
    }

    public static void assertEqualSheetNames(Workbook expected, Workbook actual) {
        Assertions.assertEquals(expected.getNumberOfSheets(), actual.getNumberOfSheets());
        for (int i = 0; i < expected.getNumberOfSheets(); i++) {
            Assertions.assertEquals(actual.getSheetName(i), actual.getSheetName(i));
        }
    }

    // endregion

    // region execution time assertions

    public static void assertFaster(final Runnable runnable, final long maxTime) {
        final long elapsed = ExecutionUtils.run(runnable).toMillis();
        if (elapsed > maxTime) {
            Assertions.fail("Expected execution within maximum " + maxTime + " ms. Actual is " + elapsed + " ms");
        }
    }

    public static void assertSlower(final Runnable runnable, final long minTime) {
        final long elapsed = ExecutionUtils.run(runnable).toMillis();
        if (elapsed < minTime) {
            Assertions.fail("Expected execution within minimum " + minTime + " ms. Actual is " + elapsed + " ms");
        }
    }

    // endregion

    public static ContextConsumer<AssertableApplicationContext> createBindValidationExceptionAssertConsumer(final String message) {
        return context -> assertContextStartupFailedWithBindValidationException(context, message);
    }

    public static void assertContextStartupFailedWithBindValidationException(final AssertableApplicationContext context, final String message) {
        final Throwable startupFailure = context.getStartupFailure();

        Assertions.assertNotNull(startupFailure, "context startup not failed as expected");

        final BindValidationException bindValidationException = (BindValidationException) startupFailure.getCause().getCause();
        final List<ObjectError> errors = bindValidationException.getValidationErrors().getAllErrors();

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(message, errors.get(0).getDefaultMessage());
    }

    // region exceptions assertions

    public static <T extends Throwable> void assertThrowsWithMessagePattern(
            final Executable executable,
            final Class<T> expectedType,
            final String expectedMessagePattern
    ) {
        final Throwable throwable = Assertions.assertThrows(expectedType, executable);
        final Pattern pattern = Pattern.compile(expectedMessagePattern);
        final Matcher matcher = pattern.matcher(throwable.getMessage());

        if (!matcher.matches()) {
            final String message = "pattern:" + System.lineSeparator()
                    + expectedMessagePattern + System.lineSeparator()
                    + "actual:" + System.lineSeparator()
                    + throwable.getMessage();
            Assertions.fail(message);
        }
    }

    // endregion

    // region validation assertions

    public static <T> void assertNoViolations(T object) {
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(object);

        Assertions.assertTrue(violations.isEmpty());
    }

    public static <T> void assertViolation(T object, final String expectedMessage) {
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(object);

        Assertions.assertEquals(1, violations.size(), "expected single violation");
        Assertions.assertEquals(expectedMessage, violations.iterator().next().getMessage());
    }

    // endregion

    public static void assertContextStartupFailed(final AssertableApplicationContext context, final String... messageSubstrings) {
        final Throwable startupFailure = context.getStartupFailure();

        Assertions.assertNotNull(startupFailure);

        final String message = getBindValidationExceptionMessage(startupFailure);
        for (final String substring : messageSubstrings) {
            if (!message.contains(substring)) {
                String failMessage = String.format("Expected but not found substring '%s' in message '%s'", substring, message);
                Assertions.fail(failMessage);
            }
        }
    }

    private String getBindValidationExceptionMessage(final Throwable startupFailure) {
        final BindValidationException bindValidationException = (BindValidationException) startupFailure.getCause().getCause();
        return bindValidationException.getMessage();
    }

}
package ru.obukhov.trader.test.utils;

import com.google.protobuf.Timestamp;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
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
import ru.obukhov.trader.market.model.Money;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyMapper;

import java.awt.Color;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class AssertUtils {

    private static final ColorMapper COLOR_MAPPER = Mappers.getMapper(ColorMapper.class);
    private static final MoneyMapper MONEY_MAPPER = Mappers.getMapper(MoneyMapper.class);
    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);

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

    public static void assertEquals(@Nullable final ru.tinkoff.piapi.contract.v1.MoneyValue expected, final BigDecimal actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (!DecimalUtils.numbersEqual(actual, MONEY_MAPPER.moneyValueToBigDecimal(expected))) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(Double expected, ru.tinkoff.piapi.contract.v1.MoneyValue actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (!DecimalUtils.numbersEqual(MONEY_MAPPER.moneyValueToBigDecimal(actual), expected)) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(@Nullable final BigDecimal expected, final ru.tinkoff.piapi.contract.v1.MoneyValue actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (!DecimalUtils.numbersEqual(expected, MONEY_MAPPER.moneyValueToBigDecimal(actual))) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(@Nullable final Money expected, final Money actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (!DecimalUtils.numbersEqual(actual.value(), expected.value()) || actual.currency() != expected.currency()) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(@Nullable final Timestamp expected, @Nullable final OffsetDateTime actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (!DATE_TIME_MAPPER.timestampToOffsetDateTime(expected).equals(actual)) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(@Nullable final OffsetDateTime expected, @Nullable final Timestamp actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (!DATE_TIME_MAPPER.offsetDateTimeToTimestamp(expected).equals(actual)) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(final PortfolioPosition portfolioPosition1, final PortfolioPosition portfolioPosition2) {
        Assertions.assertEquals(portfolioPosition1.figi(), portfolioPosition2.figi());
        Assertions.assertEquals(portfolioPosition1.instrumentType(), portfolioPosition2.instrumentType());
        assertEquals(portfolioPosition1.quantity(), portfolioPosition2.quantity());
        assertEquals(portfolioPosition1.averagePositionPrice(), portfolioPosition2.averagePositionPrice());
        assertEquals(portfolioPosition1.expectedYield(), portfolioPosition2.expectedYield());
        assertEquals(portfolioPosition1.currentPrice(), portfolioPosition2.currentPrice());
        assertEquals(portfolioPosition1.quantityLots(), portfolioPosition2.quantityLots());
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

    public static void assertMatchesRegex(final String value, final String regex) {
        final Matcher matcher = Pattern.compile(regex).matcher(value);
        if (!matcher.matches()) {
            Assertions.fail(value + System.lineSeparator() + "does not matches regex:" + System.lineSeparator() + regex);
        }
    }

    // region collections assertions

    public static void assertEquals(final Collection<?> expected, final Collection<?> actual) {
        assertCollectionSize(expected, actual);

        final StringBuilder messageBuilder = new StringBuilder();
        final Iterator<?> expectedIterator = expected.iterator();
        final Iterator<?> actualIterator = actual.iterator();
        int index = 0;
        while (expectedIterator.hasNext()) {
            final Object expectedValue = expectedIterator.next();
            final Object actualValue = actualIterator.next();
            if (expectedValue instanceof Collection expectedCollection && actualValue instanceof Collection actualCollection) {
                assertEquals(expectedCollection, actualCollection);
            } else if (expectedValue instanceof BigDecimal expectedBigDecimal && actualValue instanceof BigDecimal actualBigDecimal) {
                if (!DecimalUtils.numbersEqual(actualBigDecimal, expectedBigDecimal)) {
                    messageBuilder.append(getErrorMessage(expectedValue, actualValue, index))
                            .append(System.lineSeparator());
                }
            } else if (expectedValue instanceof ru.tinkoff.piapi.core.models.Money expectedMoney && actualValue instanceof ru.tinkoff.piapi.core.models.Money actualMoney) {
                if (!equals(expectedMoney, actualMoney)) {
                    messageBuilder.append(getErrorMessage(expectedValue, actualValue, index))
                            .append(System.lineSeparator());
                }
            } else if (expectedValue instanceof Order expectedOrder && actualValue instanceof Order actualOrder) {
                if (!equals(expectedOrder, actualOrder)) {
                    messageBuilder.append(getErrorMessage(expectedValue, actualValue, index))
                            .append(System.lineSeparator());
                }
            } else if (expectedValue instanceof PortfolioPosition expectedPosition && actualValue instanceof PortfolioPosition actualPosition) {
                if (!equals(expectedPosition, actualPosition)) {
                    messageBuilder.append(getErrorMessage(expectedValue, actualValue, index))
                            .append(System.lineSeparator());
                }
            } else if (!Objects.equals(expectedValue, actualValue)) {
                messageBuilder.append(getErrorMessage(expectedValue, actualValue, index))
                        .append(System.lineSeparator());
            }
            index++;
        }

        final String message = messageBuilder.toString();
        if (!message.isEmpty()) {
            Assertions.fail(message);
        }
    }

    private static void assertCollectionSize(final Collection<?> expected, final Collection<?> actual) {
        if (expected.size() != actual.size()) {
            final String message = String.format("expected collection of size: <%s> but was: <%s>", expected.size(), actual.size());
            Assertions.fail(message);
        }
    }

    public static boolean equals(final ru.tinkoff.piapi.core.models.Money money1, final ru.tinkoff.piapi.core.models.Money money2) {
        return money1.getCurrency().equals(money2.getCurrency())
                && DecimalUtils.numbersEqual(money1.getValue(), money2.getValue());
    }

    public static boolean equals(final Order order1, final Order order2) {
        return Objects.equals(order1.orderId(), order2.orderId())
                && Objects.equals(order1.executionReportStatus(), order2.executionReportStatus())
                && Objects.equals(order1.quantityLots(), order2.quantityLots())
                && DecimalUtils.numbersEqual(order1.initialOrderPrice(), order2.initialOrderPrice())
                && DecimalUtils.numbersEqual(order1.totalOrderAmount(), order2.totalOrderAmount())
                && DecimalUtils.numbersEqual(order1.averagePositionPrice(), order2.averagePositionPrice())
                && DecimalUtils.numbersEqual(order1.commission(), order2.commission())
                && Objects.equals(order1.figi(), order2.figi())
                && Objects.equals(order1.direction(), order2.direction())
                && DecimalUtils.numbersEqual(order1.initialSecurityPrice(), order2.initialSecurityPrice())
                && DecimalUtils.numbersEqual(order1.serviceCommission(), order2.serviceCommission())
                && Objects.equals(order1.currency(), order2.currency())
                && Objects.equals(order1.type(), order2.type())
                && Objects.equals(order1.dateTime(), order2.dateTime());
    }

    private static boolean equals(final PortfolioPosition position1, final PortfolioPosition position2) {
        return position1.figi().equals(position2.figi())
                && position1.instrumentType().equals(position2.instrumentType())
                && DecimalUtils.numbersEqual(position1.quantity(), position2.quantity())
                && equals(position1.averagePositionPrice(), position2.averagePositionPrice())
                && DecimalUtils.numbersEqual(position1.expectedYield(), position2.expectedYield())
                && equals(position1.currentPrice(), position2.currentPrice())
                && DecimalUtils.numbersEqual(position1.quantityLots(), position2.quantityLots());
    }

    private static boolean equals(final Money money1, final Money money2) {
        return money1.currency().equals(money2.currency())
                && DecimalUtils.numbersEqual(money1.value(), money1.value());
    }

    private static String getErrorMessage(final Object expectedValue, final Object actualValue, final int index) {
        return String.format("expected: <%s> at position <%s> but was: <%s>", expectedValue, index, actualValue);
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

    public static <T extends Throwable> void assertThrowsWithMessage(
            final Class<T> expectedType,
            final Executable executable,
            final String expectedMessage
    ) {
        final Throwable throwable = Assertions.assertThrows(expectedType, executable);
        Assertions.assertEquals(expectedMessage, throwable.getMessage());
    }

    public static <T extends Throwable> void assertThrowsWithMessageSubStrings(
            final Class<T> expectedType,
            final Executable executable,
            final String... expectedMessageSubstrings
    ) {
        final Throwable throwable = Assertions.assertThrows(expectedType, executable);
        final String message = throwable.getMessage();
        for (String expectedSubstring : expectedMessageSubstrings) {
            Assertions.assertTrue(message.contains(expectedSubstring));
        }
    }

    public static <T extends Throwable> void assertThrowsWithMessagePattern(
            final Class<T> expectedType,
            final Executable executable,
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
        try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            final Set<ConstraintViolation<Object>> violations = validatorFactory.getValidator().validate(object);

            Assertions.assertTrue(violations.isEmpty());
        }
    }

    public static <T> void assertViolation(T object, final String expectedMessage) {
        try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            final Set<ConstraintViolation<Object>> violations = validatorFactory.getValidator().validate(object);

            Assertions.assertEquals(1, violations.size(), "expected single violation");
            Assertions.assertEquals(expectedMessage, violations.iterator().next().getMessage());
        }
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
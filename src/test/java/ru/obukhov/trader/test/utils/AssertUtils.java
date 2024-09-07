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
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.test.utils.matchers.PositionMatcher;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Position;

import java.awt.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class AssertUtils {

    private static final ColorMapper COLOR_MAPPER = Mappers.getMapper(ColorMapper.class);

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

    public static void assertEquals(@Nullable final Double expected, @Nullable final MoneyValue actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (actual != null && !DecimalUtils.numbersEqual(DecimalUtils.newBigDecimal(actual), expected)) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(@Nullable final BigDecimal expected, @Nullable final MoneyValue actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else if (actual != null && !DecimalUtils.numbersEqual(expected, DecimalUtils.newBigDecimal(actual))) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(@Nullable final Money expected, @Nullable final Money actual) {
        if (!equals(expected, actual)) {
            Assertions.fail(String.format("expected: <%s> but was: <%s>", expected, actual));
        }
    }

    public static void assertEquals(final Position position1, final Position position2) {
        Assertions.assertEquals(position1.getFigi(), position2.getFigi());
        Assertions.assertEquals(position1.getInstrumentType(), position2.getInstrumentType());
        assertEquals(position1.getQuantity(), position2.getQuantity());
        assertEquals(position1.getAveragePositionPrice(), position2.getAveragePositionPrice());
        assertEquals(position1.getExpectedYield(), position2.getExpectedYield());
        assertEquals(position1.getCurrentNkd(), position2.getCurrentNkd());
        assertEquals(position1.getCurrentPrice(), position2.getCurrentPrice());
        assertEquals(position1.getAveragePositionPriceFifo(), position2.getAveragePositionPriceFifo());
        assertEquals(position1.getQuantityLots(), position2.getQuantityLots());
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

    public static <K, V> void assertEquals(
            final SequencedMap<? extends K, ? extends V> expected,
            final SequencedMap<? extends K, ? extends V> actual
    ) {
        final SequencedSet<?> actualEntries = (SequencedSet<?>) actual.entrySet();
        final SequencedSet<?> expectedEntries = (SequencedSet<?>) expected.entrySet();
        assertEquals(expectedEntries, actualEntries);
    }

    // endregion

    public static void assertRangeInclusive(long expectedMin, long expectedMax, long actual) {
        if (actual < expectedMin || actual > expectedMax) {
            Assertions.fail("Expected value within range [" + expectedMin + ";" + expectedMax + "], but got " + actual);
        }
    }

    public static void assertMatchesRegex(final String value, final String regex) {
        final Matcher matcher = Pattern.compile(regex).matcher(value);
        if (!matcher.matches()) {
            Assertions.fail(value + System.lineSeparator() + "does not matches regex:" + System.lineSeparator() + regex);
        }
    }

    // region collections assertions

    public static void assertEquals(final SequencedCollection<?> expected, final SequencedCollection<?> actual) {
        assertCollectionSize(expected, actual);

        final StringBuilder messageBuilder = new StringBuilder();
        final Iterator<?> expectedIterator = expected.iterator();
        final Iterator<?> actualIterator = actual.iterator();
        int index = 0;
        while (expectedIterator.hasNext()) {
            final Object expectedValue = expectedIterator.next();
            final Object actualValue = actualIterator.next();
            if (expectedValue instanceof SequencedCollection<?> expectedCollection
                    && actualValue instanceof SequencedCollection<?> actualCollection) {
                assertEquals(expectedCollection, actualCollection);
            } else if (expectedValue instanceof BigDecimal expectedBigDecimal && actualValue instanceof BigDecimal actualBigDecimal) {
                if (!DecimalUtils.numbersEqual(actualBigDecimal, expectedBigDecimal)) {
                    messageBuilder.append(getErrorMessage(expectedValue, actualValue, index))
                            .append(System.lineSeparator());
                }
            } else if (expectedValue instanceof ru.tinkoff.piapi.core.models.Money expectedMoney
                    && actualValue instanceof ru.tinkoff.piapi.core.models.Money actualMoney) {
                if (!equals(expectedMoney, actualMoney)) {
                    messageBuilder.append(getErrorMessage(expectedValue, actualValue, index))
                            .append(System.lineSeparator());
                }
            } else if (expectedValue instanceof OrderState expectedOrderState && actualValue instanceof OrderState actualOrderState) {
                if (!equals(expectedOrderState, actualOrderState)) {
                    messageBuilder.append(getErrorMessage(expectedValue, actualValue, index))
                            .append(System.lineSeparator());
                }
            } else if (expectedValue instanceof Position expectedPosition && actualValue instanceof Position actualPosition) {
                if (!equals(expectedPosition, actualPosition)) {
                    messageBuilder.append(getErrorMessage(expectedValue, actualValue, index))
                            .append(System.lineSeparator());
                }
            } else if (!Objects.equals(expectedValue, actualValue)) {
                messageBuilder.append(getErrorMessage(expectedValue, actualValue, index))
                        .append(System.lineSeparator());
            }
            index++;
            if (messageBuilder.length() > 1000) {
                messageBuilder.append("...");
                break;
            }
        }

        if (!messageBuilder.isEmpty()) {
            Assertions.fail(messageBuilder.toString());
        }
    }

    private static void assertCollectionSize(final Collection<?> expected, final Collection<?> actual) {
        if (expected.size() != actual.size()) {
            final String message = String.format("expected collection of size: <%s> but was: <%s>", expected.size(), actual.size());
            Assertions.fail(message);
        }
    }

    public static boolean equals(final Money money1, final Money money2) {
        return money1 == money2
                || money1 != null && money2 != null
                && money1.getCurrency().equals(money2.getCurrency()) && DecimalUtils.numbersEqual(money1.getValue(), money2.getValue());
    }

    public static boolean equals(final MoneyValue money1, final MoneyValue money2) {
        return money1 == money2
                || money1 != null && money2 != null
                && money1.getCurrency().equals(money2.getCurrency())
                && money1.getUnits() == money2.getUnits()
                && money1.getNano() == money2.getNano();
    }

    private static boolean equals(final OrderState state1, final OrderState state2) {
        return Objects.equals(state1.getOrderId(), state2.getOrderId())
                && state1.getExecutionReportStatus() == state2.getExecutionReportStatus()
                && state1.getLotsRequested() == state2.getLotsRequested()
                && state1.getLotsExecuted() == state2.getLotsExecuted()
                && equals(state1.getInitialOrderPrice(), state2.getInitialOrderPrice())
                && equals(state1.getExecutedOrderPrice(), state2.getExecutedOrderPrice())
                && equals(state1.getTotalOrderAmount(), state2.getTotalOrderAmount())
                && equals(state1.getAveragePositionPrice(), state2.getAveragePositionPrice())
                && equals(state1.getInitialCommission(), state2.getInitialCommission())
                && equals(state1.getExecutedCommission(), state2.getExecutedCommission())
                && Objects.equals(state1.getFigi(), state2.getFigi())
                && state1.getDirection() == state2.getDirection()
                && equals(state1.getInitialSecurityPrice(), state2.getInitialSecurityPrice())
                && Objects.equals(state1.getStagesList(), state2.getStagesList())
                && equals(state1.getServiceCommission(), state2.getServiceCommission())
                && Objects.equals(state1.getCurrency(), state2.getCurrency())
                && state1.getOrderType() == state2.getOrderType()
                && Objects.equals(state1.getOrderDate(), state2.getOrderDate())
                && Objects.equals(state1.getInstrumentUid(), state2.getInstrumentUid());
    }

    private static boolean equals(final Position position1, final Position position2) {
        return PositionMatcher.of(position1).matches(position2);
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

    public static void assertCell(
            final ExtendedCell cell,
            final ExtendedRow extendedRow,
            final int column,
            final CellType cellType,
            final String cellStyleName,
            final Object value
    ) {
        Assertions.assertSame(extendedRow, cell.getRow());
        Assertions.assertEquals(column, cell.getColumnIndex());
        assertCell(cell, cellType, cellStyleName, value);
    }

    public static void assertCell(
            final ExtendedCell cell,
            final CellType expectedCellType,
            final String expectedCellStyleName,
            final Object expectedValue
    ) {
        Assertions.assertEquals(expectedCellType, cell.getCellType());
        final CellStyle expectedCellStyle = cell.getWorkbook().getCellStyle(expectedCellStyleName);
        Assertions.assertEquals(expectedCellStyle, cell.getCellStyle());

        assertCellValue(cell, expectedValue);
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
                switch (value) {
                    case null -> AssertUtils.assertEquals(NumberUtils.DOUBLE_ZERO, cell.getNumericCellValue());
                    case BigDecimal bigDecimalValue -> assertCellValue(cell, bigDecimalValue);
                    case Double doubleValue -> assertCellValue(cell, doubleValue);
                    case Integer integerValue -> assertCellValue(cell, integerValue);
                    case Long longValue -> assertCellValue(cell, longValue);
                    case LocalDateTime localDateTimeValue -> assetCellValue(cell, localDateTimeValue);
                    case OffsetDateTime offsetDateTimeValue -> assertCellValue(cell, offsetDateTimeValue);
                    case Timestamp timestamp -> assertCellValue(cell, timestamp);
                    default -> throw new IllegalArgumentException("Unexpected value " + value);
                }
                break;

            case BOOLEAN:
                assertCellValue(cell, (Boolean) value);
                break;

            case FORMULA:
                assertCellFormulaValue(cell, (String) value);
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
        final Date expectedValue = Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
        Assertions.assertEquals(expectedValue, cell.getDateCellValue());
    }

    public static void assertCellValue(final Cell cell, final OffsetDateTime value) {
        final Date expectedValue = Date.from(value.toInstant());
        Assertions.assertEquals(expectedValue, cell.getDateCellValue());
    }

    public static void assertCellValue(final Cell cell, final Timestamp value) {
        final Date expectedValue = Date.from(TimestampUtils.toInstant(value));
        Assertions.assertEquals(expectedValue, cell.getDateCellValue());
    }

    public static void assertCellFormulaValue(final Cell cell, final String value) {
        final String exceptedValue = value == null ? StringUtils.EMPTY : value;
        Assertions.assertEquals(exceptedValue, cell.getCellFormula());
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
        Assertions.assertEquals(message, errors.getFirst().getDefaultMessage());
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
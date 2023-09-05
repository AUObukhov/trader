package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.util.function.Supplier;
import java.util.stream.Stream;

class SingleItemCollectorUnitTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "test value")
    void returnsValue_whenSingleValue(final String value) {
        final SingleItemCollector<String> singleItemCollector = new SingleItemCollector<>();

        final String actualResult = Stream.of(value).collect(singleItemCollector);

        Assertions.assertEquals(value, actualResult);
    }

    @Test
    void throwsIllegalArgumentException_whenNoValues_andNoExceptionSupplier() {
        final Stream<String> stream = Stream.of();
        final SingleItemCollector<String> singleItemCollector = new SingleItemCollector<>();

        final Executable executable = () -> System.out.println(stream.collect(singleItemCollector));
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "Expected single item. No items found.");
    }

    @Test
    void throwsSuppliedException_whenNoValues_andSingleExceptionSupplier() {
        final Stream<String> stream = Stream.of();
        final String exceptionMessage = "test exception";
        final SingleItemCollector<String> singleItemCollector = new SingleItemCollector<>(() -> new TestException(exceptionMessage));

        final Executable executable = () -> System.out.println(stream.collect(singleItemCollector));
        AssertUtils.assertThrowsWithMessage(TestException.class, executable, exceptionMessage);
    }

    @Test
    void throwsSuppliedException_whenNoValues_andTwoExceptionSuppliers() {
        final Stream<String> stream = Stream.of();
        final String exceptionMessage = "test exception";
        final Supplier<TestException> noItemsExceptionSupplier = () -> new TestException(exceptionMessage);
        final Supplier<TestException> multipleItemsExceptionSupplier = () -> new TestException("another test exception");
        final SingleItemCollector<String> singleItemCollector = new SingleItemCollector<>(noItemsExceptionSupplier, multipleItemsExceptionSupplier);

        final Executable executable = () -> System.out.println(stream.collect(singleItemCollector));
        AssertUtils.assertThrowsWithMessage(TestException.class, executable, exceptionMessage);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forThrowsException_whenMultipleValues() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("test value", null),
                Arguments.of("test value", "test value"),
                Arguments.of("test value 1", "test value 2")
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forThrowsException_whenMultipleValues")
    void throwsIllegalArgumentException_whenMultipleValues_andNoExceptionSupplier(final String value1, final String value2) {
        final Stream<String> stream = Stream.of(value1, value2);
        final SingleItemCollector<String> singleItemCollector = new SingleItemCollector<>();

        final Executable executable = () -> System.out.println(stream.collect(singleItemCollector));
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "Expected single item. Multiple items found.");
    }

    @ParameterizedTest
    @MethodSource("getData_forThrowsException_whenMultipleValues")
    void throwsSuppliedException_whenMultipleValues_andSingleExceptionSupplier(final String value1, final String value2) {
        final Stream<String> stream = Stream.of(value1, value2);
        final String exceptionMessage = "test exception";
        final SingleItemCollector<String> singleItemCollector = new SingleItemCollector<>(() -> new TestException(exceptionMessage));

        final Executable executable = () -> System.out.println(stream.collect(singleItemCollector));
        AssertUtils.assertThrowsWithMessage(TestException.class, executable, exceptionMessage);
    }

    @ParameterizedTest
    @MethodSource("getData_forThrowsException_whenMultipleValues")
    void throwsSuppliedException_whenMultipleValues_andTwoExceptionSuppliers(final String value1, final String value2) {
        final Stream<String> stream = Stream.of(value1, value2);
        final String exceptionMessage = "test exception";
        final Supplier<TestException> noItemsExceptionSupplier = () -> new TestException("another test exception");
        final Supplier<TestException> multipleItemsExceptionSupplier = () -> new TestException(exceptionMessage);
        final SingleItemCollector<String> singleItemCollector = new SingleItemCollector<>(noItemsExceptionSupplier, multipleItemsExceptionSupplier);

        final Executable executable = () -> System.out.println(stream.collect(singleItemCollector));
        AssertUtils.assertThrowsWithMessage(TestException.class, executable, exceptionMessage);
    }

    private static class TestException extends RuntimeException {
        public TestException(String message) {
            super(message);
        }
    }

}
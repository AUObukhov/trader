package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.core.models.Money;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class MoneyUtilsUnitTest {

    // region getSum tests

    @Test
    void getSum_throwsIllegalArgumentException_whenMoneysIsEmpty() {
        final Executable executable = () -> MoneyUtils.getSum(Collections.emptyList());
        final String expectedMessage = "moneys must be not empty and have same currencies";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void getSum_throwsIllegalArgumentException_whenCurrenciesAreDifferent() {
        final Money money1 = TestData.newMoney(10, Currencies.USD);
        final Money money2 = TestData.newMoney(20, Currencies.RUB);

        final Executable executable = () -> MoneyUtils.getSum(List.of(money1, money2));
        final String expectedMessage = "moneys must be not empty and have same currencies";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetSum_withList() {
        return Stream.of(
                Arguments.of(List.of(1.234567), 1.234567),

                Arguments.of(List.of(1.234567, 0.0), 1.234567),
                Arguments.of(List.of(10.0, 5.0), 15),
                Arguments.of(List.of(-1.234567, 1.234567), 0),
                Arguments.of(List.of(1.234567, 2.345678), 3.580245),
                Arguments.of(List.of(-1.234567, -2.345678), -3.580245),

                Arguments.of(
                        List.of(65.75, 58.51, -96.37, -57.45, -63.09, -28.95, -92.69, 41.09, 55.55, -18.76),
                        -136.41
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetSum_withList")
    void getSum_withList(
            final List<Double> values,
            final double expectedSum
    ) {
        final String currency = Currencies.RUB;
        final List<Money> moneys = values.stream().map(value -> TestData.newMoney(value, currency)).toList();

        final Money sum = MoneyUtils.getSum(moneys);

        AssertUtils.assertEquals(expectedSum, sum.getValue());
        Assertions.assertEquals(currency, sum.getCurrency());
    }

    // endregion

    // region getAverage tests

    @Test
    void getAverage_throwsIllegalArgumentException_whenMoneysIsEmpty() {
        final Executable executable = () -> MoneyUtils.getAverage(Collections.emptyList(), Collections.emptyList());
        final String expectedMessage = "moneys must be not empty and have same currencies";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void getAverage_throwsIllegalArgumentException_whenCurrenciesAreDifferent() {
        final Money money1 = TestData.newMoney(10, Currencies.USD);
        final Money money2 = TestData.newMoney(20, Currencies.RUB);

        final Executable executable = () -> MoneyUtils.getAverage(List.of(money1, money2), List.of(1, 2));
        final String expectedMessage = "moneys must be not empty and have same currencies";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverage_withList() {
        return Stream.of(
                Arguments.of(List.of(1.234567), List.of(1), 1.234567),

                Arguments.of(List.of(10.0, 5.0), List.of(1, 0), 10),
                Arguments.of(List.of(-10.0, 10.0), List.of(1, 0), -10),
                Arguments.of(List.of(1.234567, 2.345678), List.of(1, 0), 1.234567),

                Arguments.of(List.of(10.0, 5.0), List.of(0, 1), 5),
                Arguments.of(List.of(-10.0, 10.0), List.of(0, 1), 10),
                Arguments.of(List.of(1.234567, 2.345678), List.of(0, 1), 2.345678),

                Arguments.of(List.of(10.0, 5.0), List.of(1, 1), 7.5),
                Arguments.of(List.of(-10.0, 10.0), List.of(1, 1), 0),
                Arguments.of(List.of(-15.0, -10.0), List.of(1, 1), -12.5),
                Arguments.of(List.of(10.0, 10.0), List.of(1, 1), 10),
                Arguments.of(List.of(1.234567, 2.345678), List.of(1, 1), 1.790122500),
                Arguments.of(List.of(1.234567, 9.87654321), List.of(1, 1), 5.555555105),

                Arguments.of(List.of(10.0, 5.0), List.of(14, 14), 7.5),
                Arguments.of(List.of(-10.0, 10.0), List.of(14, 14), 0),
                Arguments.of(List.of(-15.0, -10.0), List.of(14, 14), -12.5),
                Arguments.of(List.of(10.0, 10.0), List.of(14, 14), 10),
                Arguments.of(List.of(1.234567, 2.345678), List.of(14, 14), 1.790122500),
                Arguments.of(List.of(1.234567, 9.87654321), List.of(14, 14), 5.555555105),

                Arguments.of(List.of(10.0, 5.0), List.of(14, 3), 9.117647059),
                Arguments.of(List.of(-10.0, 10.0), List.of(14, 3), -6.470588235),
                Arguments.of(List.of(-15.0, -10.0), List.of(14, 3), -14.117647059),
                Arguments.of(List.of(10.0, 10.0), List.of(14, 3), 10),
                Arguments.of(List.of(1.234567, 2.345678), List.of(14, 3), 1.430645412),

                Arguments.of(
                        List.of(65.75, 58.51, -96.37, -57.45, -63.09, -28.95, -92.69, 41.09, 55.55, -18.76),
                        List.of(12, 14, 47, 14, 54, 30, 39, 66, 90, 30),
                        -11.280757576
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverage_withList")
    void getAverage_withList(
            final List<Double> values,
            final List<Integer> quantities,
            final double expectedAverage
    ) {
        final String currency = Currencies.RUB;
        final List<Money> moneys = values.stream().map(value -> TestData.newMoney(value, currency)).toList();

        final Money average = MoneyUtils.getAverage(moneys, quantities);

        AssertUtils.assertEquals(expectedAverage, average.getValue());
        Assertions.assertEquals(currency, average.getCurrency());
    }

    // endregion

}

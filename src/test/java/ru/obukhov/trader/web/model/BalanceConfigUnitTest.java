package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.quartz.CronExpression;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.stream.Stream;

class BalanceConfigUnitTest {

    // region equals test

    static Stream<Arguments> getData_forEquals() throws ParseException {
        return Stream.of(
                Arguments.of(
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        true
                ),
                Arguments.of(
                        new BalanceConfig(BigDecimal.TEN, null, null),
                        new BalanceConfig(BigDecimal.TEN, null, null),
                        true
                ),
                Arguments.of(
                        new BalanceConfig(
                                BigDecimal.valueOf(10.1).setScale(10, RoundingMode.HALF_UP),
                                BigDecimal.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        new BalanceConfig(
                                BigDecimal.valueOf(10.1).setScale(5, RoundingMode.HALF_UP),
                                BigDecimal.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        true
                ),
                Arguments.of(
                        new BalanceConfig(
                                BigDecimal.TEN,
                                BigDecimal.valueOf(1.1).setScale(10, RoundingMode.HALF_UP),
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        new BalanceConfig(
                                BigDecimal.TEN,
                                BigDecimal.valueOf(1.1).setScale(10, RoundingMode.HALF_UP),
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        true
                ),

                Arguments.of(
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        null,
                        false
                ),
                Arguments.of(
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")).toString(),
                        false
                ),

                Arguments.of(
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        new BalanceConfig(BigDecimal.ONE, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        false
                ),
                Arguments.of(
                        new BalanceConfig(null, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        false
                ),
                Arguments.of(
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        new BalanceConfig(null, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        false
                ),

                Arguments.of(
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.TEN, new CronExpression("0 0 0 1 * ?")),
                        false
                ),
                Arguments.of(
                        new BalanceConfig(BigDecimal.TEN, null, new CronExpression("0 0 0 1 * ?")),
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        false
                ),
                Arguments.of(
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        new BalanceConfig(BigDecimal.TEN, null, new CronExpression("0 0 0 1 * ?")),
                        false
                ),

                Arguments.of(
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 2 * ?")),
                        false
                ),
                Arguments.of(
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, null),
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        false
                ),
                Arguments.of(
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, new CronExpression("0 0 0 1 * ?")),
                        new BalanceConfig(BigDecimal.TEN, BigDecimal.ONE, null),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forEquals")
    void equalsTest(BalanceConfig balanceConfig1, Object balanceConfig2, boolean expectedResult) {
        Assertions.assertEquals(expectedResult, balanceConfig1.equals(balanceConfig2));
    }

    // endregion

    @Test
    void hashCode_returnsEqualsCodes_whenObjectsAreEqual() throws ParseException {
        final BalanceConfig balanceConfig1 = new BalanceConfig(
                BigDecimal.TEN,
                BigDecimal.ONE,
                new CronExpression("0 0 0 1 * ?")
        );
        final BalanceConfig balanceConfig2 = new BalanceConfig(
                BigDecimal.TEN,
                BigDecimal.ONE,
                new CronExpression("0 0 0 1 * ?")
        );

        Assertions.assertEquals(balanceConfig1.hashCode(), balanceConfig2.hashCode());
    }

}

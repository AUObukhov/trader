package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.util.DecimalUtils;

import java.text.ParseException;
import java.util.stream.Stream;

class BalanceConfigUnitTest {

    // region equals test

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forEquals() throws ParseException {
        return Stream.of(
                Arguments.of(
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        true
                ),
                Arguments.of(
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                null,
                                null
                        ),
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                null,
                                null
                        ),
                        true
                ),

                Arguments.of(
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        null,
                        false
                ),
                Arguments.of(
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ).toString(),
                        false
                ),

                Arguments.of(
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        new BalanceConfig(
                                DecimalUtils.ONE,
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        false
                ),
                Arguments.of(
                        new BalanceConfig(
                                null,
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        false
                ),
                Arguments.of(
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        new BalanceConfig(
                                null,
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        false
                ),

                Arguments.of(
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.setDefaultScale(10),
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        false
                ),
                Arguments.of(
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                null,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        false
                ),
                Arguments.of(
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                null,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        false
                ),

                Arguments.of(
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ZERO,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 2 * ?")
                        ),
                        false
                ),
                Arguments.of(
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                null
                        ),
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        false
                ),
                Arguments.of(
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                new CronExpression("0 0 0 1 * ?")
                        ),
                        new BalanceConfig(
                                DecimalUtils.setDefaultScale(10),
                                DecimalUtils.ONE,
                                null
                        ),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forEquals")
    void equalsTest(final BalanceConfig balanceConfig1, final Object balanceConfig2, final boolean expectedResult) {
        Assertions.assertEquals(expectedResult, balanceConfig1.equals(balanceConfig2));
    }

    // endregion

    @Test
    void hashCode_returnsEqualsCodes_whenObjectsAreEqual() throws ParseException {
        final BalanceConfig balanceConfig1 = new BalanceConfig(
                DecimalUtils.setDefaultScale(10),
                DecimalUtils.ONE,
                new CronExpression("0 0 0 1 * ?")
        );
        final BalanceConfig balanceConfig2 = new BalanceConfig(
                DecimalUtils.setDefaultScale(10),
                DecimalUtils.ONE,
                new CronExpression("0 0 0 1 * ?")
        );

        Assertions.assertEquals(balanceConfig1.hashCode(), balanceConfig2.hashCode());
    }

}

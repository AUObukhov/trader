package ru.obukhov.trader.test.utils;

import ru.obukhov.trader.common.util.DecimalUtils;
import ru.tinkoff.piapi.core.models.Money;

public class TestMoneyUtils {

    public static Money multiply(final Money multiplier, final double multiplicand) {
        return Money.builder()
                .value(DecimalUtils.multiply(multiplier.getValue(), multiplicand))
                .currency(multiplier.getCurrency())
                .build();
    }

}
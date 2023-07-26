package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DecimalUtils;

import java.math.BigDecimal;

class MoneyUnitTest {
    @Test
    void of() {
        final String currency = Currency.RUB;
        final BigDecimal value = DecimalUtils.setDefaultScale(100);

        final Money money = Money.of(currency, value);

        Assertions.assertEquals(currency, money.currency());
        Assertions.assertEquals(value, money.value());
    }
}
package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class MoneyUnitTest {
    @Test
    void of() {
        final Currency currency = Currency.RUB;
        final BigDecimal value = BigDecimal.valueOf(100);

        final Money money = Money.of(currency, value);

        Assertions.assertEquals(currency, money.currency());
        Assertions.assertEquals(value, money.value());
    }
}
package ru.obukhov.trader.common.util;

import org.springframework.util.Assert;
import ru.tinkoff.piapi.core.models.Money;

import java.math.BigDecimal;
import java.util.List;

public class MoneyUtils {

    public static Money getSum(final List<Money> moneys) {
        final long currenciesCount = moneys.stream().map(Money::getCurrency).distinct().count();
        Assert.isTrue(currenciesCount == 1, "moneys must be not empty and have same currencies");

        BigDecimal value = moneys.stream()
                .map(Money::getValue)
                .reduce(BigDecimal::add)
                .map(DecimalUtils::setDefaultScale)
                .orElseThrow();

        return Money.builder()
                .value(value)
                .currency(moneys.getFirst().getCurrency())
                .build();
    }

    public static Money getAverage(final List<Money> moneys, final List<Integer> quantities) {
        final long currenciesCount = moneys.stream().map(Money::getCurrency).distinct().count();
        Assert.isTrue(currenciesCount == 1, "moneys must be not empty and have same currencies");
        List<BigDecimal> values = moneys.stream().map(Money::getValue).toList();

        return Money.builder()
                .value(DecimalUtils.getAverage(values, quantities))
                .currency(moneys.getFirst().getCurrency())
                .build();
    }

}